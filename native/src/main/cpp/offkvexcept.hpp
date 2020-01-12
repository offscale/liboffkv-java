#pragma once

#include "jni_utils.hpp"
#include <liboffkv/liboffkv.hpp>

template <>
class ExceptionRaiser<
        ClassMapping<liboffkv::TxnFailed, decltype("io/offscale/liboffkv/TransactionFailedException"_cexpr)>
    > {
public:
    static void raise(JNIEnv* env, const liboffkv::TxnFailed& txn_failure) {
        jthrowable exc = nullptr;
        auto [clazz, constructor] =
            prepare_construction(env, "io/offscale/liboffkv/TransactionFailedException", "(I)V");
        if (clazz)
            exc = reinterpret_cast<jthrowable>(env->NewObject(clazz, constructor, txn_failure.failed_op()));

        if (env->ExceptionCheck())
            return;
        if (exc == nullptr) {
            env->FatalError("Internal");
            return;
        }

        env->Throw(exc);
    }
};


using OffkvExceptionMapper = ExceptionMapper<
    decltype("io/offscale/liboffkv/ServiceException"_cexpr),
    liboffkv::Error,
    ClassMapping<liboffkv::NoEntry, decltype("io/offscale/liboffkv/KeyNotFoundException"_cexpr)>,
    ClassMapping<liboffkv::NoChildrenForEphemeral, decltype("io/offscale/liboffkv/NoChildrenForEphemeralException"_cexpr)>,
    ClassMapping<liboffkv::EntryExists, decltype("io/offscale/liboffkv/KeyAlreadyExistsException"_cexpr)>,
    ClassMapping<liboffkv::ConnectionLoss, decltype("io/offscale/liboffkv/ConnectionLostException"_cexpr)>,
    ClassMapping<liboffkv::InvalidKey, decltype("io/offscale/liboffkv/InvalidKeyException"_cexpr)>,
    ClassMapping<liboffkv::InvalidAddress, decltype("io/offscale/liboffkv/InvalidAddressException"_cexpr)>,
    ClassMapping<liboffkv::TxnFailed, decltype("io/offscale/liboffkv/TransactionFailedException"_cexpr)>,
    ClassMapping<liboffkv::ServiceError, decltype("io/offscale/liboffkv/ServiceException"_cexpr)>
>;

#define OFFKV_RAISE_L(env, exc) OffkvExceptionMapper::raise(env, exc); return 0;
#define OFFKV_RAISE_O(env, exc) OffkvExceptionMapper::raise(env, exc); return nullptr;
#define OFFKV_RAISE_V(env, exc) OffkvExceptionMapper::raise(env, exc); return;
