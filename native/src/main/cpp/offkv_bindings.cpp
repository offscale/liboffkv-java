#include "generated/io_offscale_liboffkv_NativeClient.h"
#include <liboffkv/liboffkv.hpp>
#include <string>

class JString {
private:
    JNIEnv* env;
    jstring string;
    const char* c_str = nullptr;

public:
    JString(JNIEnv* env, jstring string)
        : env(env), string(string) {}

    operator const char*() {
        if (c_str == nullptr) {
            c_str = env->GetStringUTFChars(string, nullptr);
        }
        return c_str;
    }

    operator std::string() {
        return static_cast<const char*>(*this);
    }

    ~JString() {
        if (c_str != nullptr) {
            env->ReleaseStringUTFChars(string, c_str);
            c_str = nullptr;
        }
    }
};

class JBytes {
private:
    JNIEnv* env;
    jbyteArray bytes;
    jbyte* c_bytes = nullptr;

private:
    char* get_bytes() {
        if (c_bytes == nullptr) {
            c_bytes = env->GetByteArrayElements(bytes, nullptr);
        }
        return reinterpret_cast<char*>(c_bytes);
    }

public:
    JBytes(JNIEnv* env, jbyteArray bytes)
        : env(env), bytes(bytes) {}

    operator std::string() {
        return std::string(get_bytes(), env->GetArrayLength(bytes));
    }

    ~JBytes() {
        if (c_bytes != nullptr) {
            env->ReleaseByteArrayElements(bytes, c_bytes, JNI_ABORT);
            c_bytes = nullptr;
        }
    }
};

static jbyteArray to_java_bytes(JNIEnv* env, const std::string& data) {
    jbyteArray result = env->NewByteArray(data.size());
    void* rawdata = env->GetPrimitiveArrayCritical(result, nullptr);
    std::copy(data.begin(), data.end(), reinterpret_cast<jchar*>(rawdata));
    env->ReleasePrimitiveArrayCritical(result, rawdata, JNI_COMMIT);
    return result;
}

static jstring to_java_string(JNIEnv* env, const std::string& data) {
    return env->NewStringUTF(data.c_str());
}

static liboffkv::Client& unwrap(jlong handle) {
    return *reinterpret_cast<liboffkv::Client*>(handle);
}

static jobject make_result(JNIEnv* env, jlong version, jobject value, liboffkv::WatchHandle* watch) {
    jclass clazz = env->FindClass("io/offscale/liboffkv/ResultHandle");
    if (!clazz)
        return nullptr;

    jmethodID constructor = env->GetMethodID(clazz, "<init>", "(JLjava/lang/Object;J)V");
    if (!constructor)
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

/*
 * Class:     io_offscale_liboffkv_NativeClient
 * Method:    connect
 * Signature: (Ljava/lang/String;Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_io_offscale_liboffkv_NativeClient_connect(
    JNIEnv* env, jobject self, jstring url, jstring prefix) {
    // TODO: add error handling
    liboffkv::Client* client = liboffkv::open(JString{env, url}, JString{env, prefix}).release();
    return reinterpret_cast<jlong>(client);
}

/*
 * Class:     io_offscale_liboffkv_NativeClient
 * Method:    create
 * Signature: (JLjava/lang/String;[BZ)J
 */
JNIEXPORT jlong JNICALL Java_io_offscale_liboffkv_NativeClient_create(
    JNIEnv* env, jobject self, jlong handle, jstring key, jbyteArray value, jboolean lease) {
    // TODO: add error handling
    return unwrap(handle).create(JString{env, key}, JBytes{env, value}, lease);
}

/*
 * Class:     io_offscale_liboffkv_NativeClient
 * Method:    delete
 * Signature: (Ljava/lang/String;J)V
 */
JNIEXPORT void JNICALL Java_io_offscale_liboffkv_NativeClient_delete
    (JNIEnv* env, jobject self, jlong handle, jstring key, jlong version) {
    // TODO: add error handling
    unwrap(handle).erase(JString{env, key}, version);
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
    // TODO: add error handling
    liboffkv::ExistsResult result = unwrap(handle).exists(JString{env, key}, watch);
    return convert_result(env, std::move(result));
}

/*
 * Class:     io_offscale_liboffkv_NativeClient
 * Method:    get
 * Signature: (JLjava/lang/String;Z)Lio/offscale/liboffkv/ResultHandle;
 */
JNIEXPORT jobject JNICALL Java_io_offscale_liboffkv_NativeClient_get(
    JNIEnv* env, jobject self, jlong handle, jstring key, jboolean watch) {
    // TODO: add error handling
    liboffkv::GetResult result = unwrap(handle).get(JString{env, key}, watch);
    return convert_result(env, std::move(result));
}

/*
 * Class:     io_offscale_liboffkv_NativeClient
 * Method:    getChildren
 * Signature: (JLjava/lang/String;Z)Lio/offscale/liboffkv/ResultHandle;
 */
JNIEXPORT jobject JNICALL Java_io_offscale_liboffkv_NativeClient_getChildren(
    JNIEnv* env, jobject self, jlong handle, jstring key, jboolean watch) {
    // TODO: add error handling
    liboffkv::ChildrenResult result = unwrap(handle).get_children(JString{env, key}, watch);
    return convert_result(env, std::move(result));
}

/*
 * Class:     io_offscale_liboffkv_NativeClient
 * Method:    compareAndSet
 * Signature: (JLjava/lang/String;[BJ)J
 */
JNIEXPORT jlong JNICALL Java_io_offscale_liboffkv_NativeClient_compareAndSet(
    JNIEnv* env, jobject self, jlong handle, jstring key, jbyteArray data, jlong version) {
    // TODO: add error handling
    liboffkv::CasResult result = unwrap(handle).cas(JString{env, key}, JBytes{env, data}, version);
    return result.version;
}

/*
 * Class:     io_offscale_liboffkv_NativeClient
 * Method:    set
 * Signature: (JLjava/lang/String;[B)J
 */
JNIEXPORT jlong JNICALL Java_io_offscale_liboffkv_NativeClient_set(
    JNIEnv* env, jobject self, jlong handle, jstring key, jbyteArray data) {
    // TODO: add error handling
    return unwrap(handle).set(JString{env, key}, JBytes{env, data});
}

/*
 * Class:     io_offscale_liboffkv_NativeClient
 * Method:    waitChanges
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_io_offscale_liboffkv_NativeClient_waitChanges(
    JNIEnv* env, jobject self, jlong watch_handle) {
    // TODO: add error handling
    reinterpret_cast<liboffkv::WatchHandle*>(watch_handle)->wait();
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
