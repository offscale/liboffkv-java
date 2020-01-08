package io.offscale.liboffkv;

import java.io.Closeable;

public class OffkvClient implements Closeable {
    private final NativeClient backend = NativeClient.get();
    private final long handle;

    public OffkvClient(String url, String prefix) throws OffkvException {
        handle = backend.connect(url, prefix);
    }

    public long create(String key, byte[] value, boolean lease) {
        return backend.create(handle, key, value, lease);
    }

    public long create(String key, byte[] value) {
        return create(key, value, false);
    }

//    virtual ExistsResult exists(const String &key, bool watch = false) = 0;
//
//    virtual ChildrenResult get_children(const String &key, bool watch = false) = 0;
//
//    virtual int64_t set(const String &key, const std::string &value) = 0;
//
//    virtual GetResult get(const String &key, bool watch = false) = 0;
//
//    virtual CasResult cas(const String &key, const std::string &value, int64_t version = 0) = 0;

    public void delete(String key, long version) {
        backend.delete(key, version);
    }

    public void delete(String key) {
        delete(key, 0);
    }

//    virtual TransactionResult commit(const Transaction&) = 0;

    public void close() throws OffkvException {
        backend.free(handle);
    }
}
