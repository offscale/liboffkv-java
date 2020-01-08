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

/*
 * Class:     io_offscale_liboffkv_NativeClient
 * Method:    connect
 * Signature: (Ljava/lang/String;Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_io_offscale_liboffkv_NativeClient_connect(
    JNIEnv* env, jobject self, jstring url, jstring prefix) {
    const liboffkv::Client* client = liboffkv::open(JString{env, url}, JString{env, prefix}).release();
    return reinterpret_cast<jlong>(client);
}

/*
 * Class:     io_offscale_liboffkv_NativeClient
 * Method:    create
 * Signature: (JLjava/lang/String;[BZ)J
 */
JNIEXPORT jlong JNICALL Java_io_offscale_liboffkv_NativeClient_create
    (JNIEnv *, jobject, jlong, jstring, jbyteArray, jboolean);

/*
 * Class:     io_offscale_liboffkv_NativeClient
 * Method:    delete
 * Signature: (Ljava/lang/String;J)V
 */
JNIEXPORT void JNICALL Java_io_offscale_liboffkv_NativeClient_delete
    (JNIEnv *, jobject, jstring, jlong);

/*
 * Class:     io_offscale_liboffkv_NativeClient
 * Method:    free
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_io_offscale_liboffkv_NativeClient_free
    (JNIEnv *, jobject, jlong);
