#pragma once

#include <string>
#include <typeinfo>
#include <algorithm>
#include <stdexcept>
#include <jni.h>


template <char... chars>
class ConstexprString {
private:
    static constexpr char str[sizeof...(chars) + 1] = { chars..., '\0' };

public:
    static constexpr const char* to_string() {
        return str;
    }
};

template <class Char, Char... chars>
constexpr ConstexprString<chars...> operator"" _cexpr() { return {}; }

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

template <class Val, class... Signature>
class JFieldSignature;

template<>
class JFieldSignature<jlong> {
public:
    static constexpr const char* signature = "J";
    static jlong get(JNIEnv* env, jobject obj, jfieldID field) {
        return env->GetLongField(obj, field);
    }
};

template<>
class JFieldSignature<jint> {
public:
    static constexpr const char* signature = "I";
    static jint get(JNIEnv* env, jobject obj, jfieldID field) {
        return env->GetIntField(obj, field);
    }
};

template<>
class JFieldSignature<jboolean> {
public:
    static constexpr const char* signature = "Z";
    static jboolean get(JNIEnv* env, jobject obj, jfieldID field) {
        return env->GetIntField(obj, field);
    }
};

template<class Signature>
class JFieldSignature<jobject, Signature> {
public:
    static constexpr const char* signature = Signature::to_string();
    static jobject get(JNIEnv* env, jobject obj, jfieldID field) {
        return env->GetObjectField(obj, field);
    }
};

template<>
class JFieldSignature<jstring> {
public:
    static constexpr const char* signature = "Ljava/lang/String;";
    static jstring get(JNIEnv* env, jobject obj, jfieldID field) {
        return reinterpret_cast<jstring>(env->GetObjectField(obj, field));
    }
};

template<>
class JFieldSignature<jbyteArray> {
public:
    static constexpr const char* signature = "[B";
    static jbyteArray get(JNIEnv* env, jobject obj, jfieldID field) {
        return reinterpret_cast<jbyteArray>(env->GetObjectField(obj, field));
    }
};

template <class Val, class... Signature>
Val get_field_value(JNIEnv* env, jobject object, const char* name) {
    using Sig = JFieldSignature<Val, Signature...>;

    jclass clazz = env->GetObjectClass(object);
    jfieldID field = env->GetFieldID(clazz, name, Sig::signature);
    if (!field)
        return Val{};

    return Sig::get(env, object, field);
}

jbyteArray to_java_bytes(JNIEnv* env, const std::string& data) {
    jbyteArray result = env->NewByteArray(data.size());
    void* rawdata = env->GetPrimitiveArrayCritical(result, nullptr);
    std::copy_n(data.data(), data.size(), reinterpret_cast<jbyte*>(rawdata));
    env->ReleasePrimitiveArrayCritical(result, rawdata, JNI_COMMIT);
    return result;
}

jstring to_java_string(JNIEnv* env, const std::string& data) {
    return env->NewStringUTF(data.c_str());
}

std::pair<jclass, jmethodID> prepare_construction(JNIEnv* env, const char* classname, const char* signature) {
    jclass clazz = env->FindClass(classname);
    if (!clazz)
        return std::make_pair(nullptr, nullptr);

    jmethodID constructor = env->GetMethodID(clazz, "<init>", signature);
    if (!constructor)
        return std::make_pair(nullptr, nullptr);

    return std::make_pair(clazz, constructor);
}

jobject construct_exception(JNIEnv* env, const char* exception, const std::string& message) {
    auto [clazz, constructor] =
    prepare_construction(env, exception, "(Ljava/lang/String;)V");
    if (clazz == nullptr)
        return nullptr;

    return env->NewObject(clazz, constructor, to_java_string(env, message));
}

jobject construct_exception(JNIEnv* env, const char* exception) {
    auto [clazz, constructor] =
    prepare_construction(env, exception, "()V");
    if (clazz == nullptr)
        return nullptr;

    return env->NewObject(clazz, constructor);
}

void raise_exception(JNIEnv* env, const char* exception, const std::string& message) {
    if (env->ExceptionCheck())
        return;

    auto exc = reinterpret_cast<jthrowable>(construct_exception(env, exception, message));
    if (exc == nullptr) {
        env->FatalError("Internal");
        return;
    }

    env->Throw(exc);
}

void raise_exception(JNIEnv* env, const char* exception) {
    if (env->ExceptionCheck())
        return;

    auto exc = reinterpret_cast<jthrowable>(construct_exception(env, exception));
    if (exc == nullptr) {
        env->FatalError("Internal");
        return;
    }

    env->Throw(exc);
}

template <class CClass, class j_class>
class ClassMapping {
public:
    using CppClass = CClass;
    static constexpr const char* java_class = j_class::to_string();
};

template <class ExcMapping>
class ExceptionRaiser {
public:
    static void raise(JNIEnv* env, const typename ExcMapping::CppClass& exc) {
        raise_exception(env, ExcMapping::java_class, exc.what());
    }
};

template <class default_exception_class, class ExcBase, class... Mappings>
class ExceptionMapper;

template <class default_exception_class, class ExcBase>
class ExceptionMapper<default_exception_class, ExcBase> {
public:
    static void raise(JNIEnv* env, ExcBase& error) {
        ExceptionRaiser<ClassMapping<ExcBase, default_exception_class>>::raise(env, error);
    }
};

template <class default_exception_class, class ExcBase, class M0, class... Mappings>
class ExceptionMapper<default_exception_class, ExcBase, M0, Mappings...> {
public:
    static void raise(JNIEnv* env, ExcBase& error) {
        if (!try_raise(env, error))
            SuccessorMapper::raise(env, error);
    }

    friend class SuccessorMapper;

private:
    using SuccessorMapper = class ExceptionMapper<default_exception_class, ExcBase, Mappings...>;

    static bool try_raise(JNIEnv* env, ExcBase& error) {
        try {
            auto& cast_error = dynamic_cast<typename M0::CppClass&>(error);
            ExceptionRaiser<M0>::raise(env, cast_error);
            return true;
        } catch (std::bad_cast& miscast) {
            return false;
        }
    }
};
