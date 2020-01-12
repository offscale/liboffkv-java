#include "generated/config.hpp"
#include "jni_utils.hpp"
#include "generated/io_offscale_liboffkv_NativeClient.h"
#include "offkvexcept.hpp"

#include <liboffkv/liboffkv.hpp>
#include <string>

#ifdef ENABLE_TRACING
#include <signal.h>
#include <execinfo.h>

namespace trace {
void my_signal_handler(int signum) {
    constexpr size_t N = 30;

    void* array[N];
    size_t size = backtrace(array, N);

    fprintf(stderr, "Error: signal %d:\n", signum);
    backtrace_symbols_fd(array, size, STDERR_FILENO);
    exit(1);
}

void init() {
    ::signal(SIGSEGV, &my_signal_handler);
    ::signal(SIGABRT, &my_signal_handler);
}
}
#else
namespace trace {

void init() {
}

}
#endif


static liboffkv::Client& unwrap(jlong handle) {
    return *reinterpret_cast<liboffkv::Client*>(handle);
}

static jobject make_result(JNIEnv* env, jlong version, jobject value, liboffkv::WatchHandle* watch) {
    auto [clazz, constructor] =
    prepare_construction(env, "io/offscale/liboffkv/ResultHandle", "(JLjava/lang/Object;J)V");
    if (!clazz)
        return nullptr;

    return env->NewObject(clazz, constructor, version, value, reinterpret_cast<jlong>(watch));
}

static jobject convert_result(JNIEnv* env, liboffkv::ExistsResult&& result) {
    return make_result(env, result.version, nullptr, result.watch.release());
}

static jobject convert_result(JNIEnv* env, liboffkv::GetResult&& result) {
    jbyteArray value = to_java_bytes(env, result.value);
    return make_result(env, result.version, value, result.watch.release());
}

static jobject convert_result(JNIEnv* env, liboffkv::ChildrenResult&& result) {
    jclass str_clazz = env->FindClass("java/lang/String");
    if (!str_clazz)
        return nullptr;

    jobjectArray children = env->NewObjectArray(result.children.size(), str_clazz, nullptr);
    if (children == nullptr)
        return nullptr;

    for (size_t i = 0; i < result.children.size(); ++i) {
        env->SetObjectArrayElement(children, i, to_java_string(env, result.children[i]));
    }

    return make_result(env, 0, children, result.watch.release());
}

static jlongArray convert_result(JNIEnv* env, liboffkv::TransactionResult&& result) {
    jlongArray arr = env->NewLongArray(result.size());
    if (!arr)
        return nullptr;

    jlong* elems = env->GetLongArrayElements(arr, nullptr);
    if (!elems)
        return nullptr;

    jlong* elems_ptr = elems;
    for (liboffkv::TxnOpResult op_result : result) {
        *(elems_ptr++) = op_result.version;
    }

    env->ReleaseLongArrayElements(arr, elems, JNI_COMMIT);
    return arr;
}

static liboffkv::TxnCheck convert_txn_check(JNIEnv* env, jobject check) {
    auto key = get_field_value<jstring>(env, check, "key");
    auto value = get_field_value<jlong>(env, check, "version");
    return {JString{env, key}, value };
}

static liboffkv::TxnOp convert_txn_op(JNIEnv* env, jobject operation) {
    auto kind = get_field_value<jint>(env, operation, "kind");
    auto key = get_field_value<jstring>(env, operation, "key");
    jbyteArray value;
    jboolean lease;

    switch (kind) {
        case 0:
            // SET
            value = get_field_value<jbyteArray>(env, operation, "value");
            return liboffkv::TxnOpSet(JString{env, key}, JBytes{env, value});

        case 1:
            // CREATE
            value = get_field_value<jbyteArray>(env, operation, "value");
            lease = get_field_value<jboolean>(env, operation, "leased");
            return liboffkv::TxnOpCreate(JString{env, key}, JBytes{env, value}, lease);

        case 2:
            // DELETE
            return liboffkv::TxnOpErase(JString{env, key});

        default:
            throw std::logic_error("Bad transaction operation");
    }
}


JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    trace::init();
    return JNI_VERSION_1_2;
}

/*
 * Class:     io_offscale_liboffkv_NativeClient
 * Method:    connect
 * Signature: (Ljava/lang/String;Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_io_offscale_liboffkv_NativeClient_connect(
    JNIEnv* env, jobject self, jstring url, jstring prefix) {
    try {
        liboffkv::Client* client = liboffkv::open(JString{env, url}, JString{env, prefix}).release();
        return reinterpret_cast<jlong>(client);
    } catch (liboffkv::Error& error) {
        OFFKV_RAISE_L(env, error)
    }
}

/*
 * Class:     io_offscale_liboffkv_NativeClient
 * Method:    create
 * Signature: (JLjava/lang/String;[BZ)J
 */
JNIEXPORT jlong JNICALL Java_io_offscale_liboffkv_NativeClient_create(
    JNIEnv* env, jobject self, jlong handle, jstring key, jbyteArray value, jboolean lease) {
    try {
        return unwrap(handle).create(JString{env, key}, JBytes{env, value}, lease);
    } catch (liboffkv::Error& error) {
        OFFKV_RAISE_L(env, error)
    }
}

/*
 * Class:     io_offscale_liboffkv_NativeClient
 * Method:    delete
 * Signature: (Ljava/lang/String;J)V
 */
JNIEXPORT void JNICALL Java_io_offscale_liboffkv_NativeClient_delete
    (JNIEnv* env, jobject self, jlong handle, jstring key, jlong version) {
    try {
        unwrap(handle).erase(JString{env, key}, version);
    } catch (liboffkv::Error& error) {
        OFFKV_RAISE_V(env, error)
    }
}

/*
 * Class:     io_offscale_liboffkv_NativeClient
 * Method:    free
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_io_offscale_liboffkv_NativeClient_free(
    JNIEnv* env, jobject self, jlong handle) {
    delete reinterpret_cast<liboffkv::Client*>(handle);
}

/*
 * Class:     io_offscale_liboffkv_NativeClient
 * Method:    exists
 * Signature: (JLjava/lang/String;Z)Lio/offscale/liboffkv/ResultHandle;
 */
JNIEXPORT jobject JNICALL Java_io_offscale_liboffkv_NativeClient_exists(
    JNIEnv* env, jobject self, jlong handle, jstring key, jboolean watch) {
    try {
        liboffkv::ExistsResult result = unwrap(handle).exists(JString{env, key}, watch);
        return convert_result(env, std::move(result));
    } catch (liboffkv::Error& error) {
        OFFKV_RAISE_O(env, error)
    }
}

/*
 * Class:     io_offscale_liboffkv_NativeClient
 * Method:    get
 * Signature: (JLjava/lang/String;Z)Lio/offscale/liboffkv/ResultHandle;
 */
JNIEXPORT jobject JNICALL Java_io_offscale_liboffkv_NativeClient_get(
    JNIEnv* env, jobject self, jlong handle, jstring key, jboolean watch) {
    try {
        liboffkv::GetResult result = unwrap(handle).get(JString{env, key}, watch);
        return convert_result(env, std::move(result));
    } catch (liboffkv::Error& error) {
        OFFKV_RAISE_O(env, error)
    }
}

/*
 * Class:     io_offscale_liboffkv_NativeClient
 * Method:    getChildren
 * Signature: (JLjava/lang/String;Z)Lio/offscale/liboffkv/ResultHandle;
 */
JNIEXPORT jobject JNICALL Java_io_offscale_liboffkv_NativeClient_getChildren(
    JNIEnv* env, jobject self, jlong handle, jstring key, jboolean watch) {
    try {
        liboffkv::ChildrenResult result = unwrap(handle).get_children(JString{env, key}, watch);
        return convert_result(env, std::move(result));
    } catch (liboffkv::Error& error) {
        OFFKV_RAISE_O(env, error)
    }
}

/*
 * Class:     io_offscale_liboffkv_NativeClient
 * Method:    compareAndSet
 * Signature: (JLjava/lang/String;[BJ)J
 */
JNIEXPORT jlong JNICALL Java_io_offscale_liboffkv_NativeClient_compareAndSet(
    JNIEnv* env, jobject self, jlong handle, jstring key, jbyteArray data, jlong version) {
    try {
        liboffkv::CasResult result = unwrap(handle).cas(JString{env, key}, JBytes{env, data}, version);
        return result.version;
    } catch (liboffkv::Error& error) {
        OFFKV_RAISE_L(env, error)
    }
}

/*
 * Class:     io_offscale_liboffkv_NativeClient
 * Method:    set
 * Signature: (JLjava/lang/String;[B)J
 */
JNIEXPORT jlong JNICALL Java_io_offscale_liboffkv_NativeClient_set(
    JNIEnv* env, jobject self, jlong handle, jstring key, jbyteArray data) {
    try {
        return unwrap(handle).set(JString{env, key}, JBytes{env, data});
    } catch (liboffkv::Error& error) {
        OFFKV_RAISE_L(env, error)
    }
}


/*
 * Class:     io_offscale_liboffkv_NativeClient
 * Method:    commit
 * Signature: (J[Lio/offscale/liboffkv/txn/TransactionCheck;[Lio/offscale/liboffkv/txn/TransactionOperation;)[J
 */
JNIEXPORT jlongArray JNICALL Java_io_offscale_liboffkv_NativeClient_commit(
    JNIEnv* env, jobject self, jlong handle, jobjectArray checks, jobjectArray operations) {
    size_t num_checks = env->GetArrayLength(checks);
    size_t num_operations = env->GetArrayLength(operations);
    liboffkv::Transaction txn;
    txn.checks.reserve(num_checks);
    txn.ops.reserve(num_operations);

    for (size_t i = 0; i < num_checks; ++i) {
        txn.checks.push_back(convert_txn_check(env, env->GetObjectArrayElement(checks, i)));
    }
    try {
        for (size_t i = 0; i < num_operations; ++i) {
            txn.ops.push_back(convert_txn_op(env, env->GetObjectArrayElement(operations, i)));
        }
    } catch (std::logic_error& err) {
        raise_exception(env, "java/lang/IllegalArgumentException", err.what());
        return nullptr;
    }

    try {
        liboffkv::TransactionResult result = unwrap(handle).commit(txn);
        return convert_result(env, std::move(result));
    } catch (liboffkv::Error& exc) {
        OFFKV_RAISE_O(env, exc);
    }
}


/*
 * Class:     io_offscale_liboffkv_NativeClient
 * Method:    waitChanges
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_io_offscale_liboffkv_NativeClient_waitChanges(
    JNIEnv* env, jobject self, jlong watch_handle) {
    try {
        reinterpret_cast<liboffkv::WatchHandle*>(watch_handle)->wait();
    } catch (liboffkv::Error& error) {
        OFFKV_RAISE_V(env, error)
    }
}

/*
 * Class:     io_offscale_liboffkv_NativeClient
 * Method:    freeWatch
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_io_offscale_liboffkv_NativeClient_freeWatch(
    JNIEnv* env, jobject self, jlong watch_handle) {
    delete reinterpret_cast<liboffkv::WatchHandle*>(watch_handle);
}
