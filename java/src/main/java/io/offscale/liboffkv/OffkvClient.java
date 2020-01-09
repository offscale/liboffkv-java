package io.offscale.liboffkv;

import java.io.Closeable;

public class OffkvClient implements Closeable {
    private final NativeClient backend = NativeClient.getInstance();
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

    public ExistsResult exists(String key, boolean watch) {
        return new ExistsResult(backend.exists(handle, key, watch));
    }

    public ExistsResult exists(String key) {
        return exists(key, false);
    }

    public ChildrenResult getChildren(String key, boolean watch) {
        return new ChildrenResult(backend.getChildren(handle, key, watch));
    }

    public ChildrenResult getChildren(String key) {
        return getChildren(key, false);
    }

    public long set(String key, byte[] value) {
        return backend.set(handle, key, value);
    }

    public GetResult get(String key, boolean watch) {
        return new GetResult(backend.get(handle, key, watch));
    }

    public GetResult get(String key) {
        return get(key, false);
    }


    public long compareAndSet(String key, byte[] value, long version) {
        return backend.compareAndSet(handle, key, value, version);
    }

    public void delete(String key, long version) {
        backend.delete(handle, key, version);
    }

    public void delete(String key) {
        delete(key, 0);
    }

    // TODO
    //    virtual TransactionResult commit(const Transaction&) = 0;

    public void close() throws OffkvException {
        backend.free(handle);
    }
}
