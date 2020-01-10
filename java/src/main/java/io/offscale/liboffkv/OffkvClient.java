package io.offscale.liboffkv;

import java.io.Closeable;
import java.net.URISyntaxException;

public class OffkvClient implements Closeable {
    private final NativeClient backend = NativeClient.getInstance();
    private final long handle;

    public OffkvClient(String url, String prefix) throws URISyntaxException, OffkvException {
        try {
            handle = backend.connect(url, prefix);
        } catch (InvalidAddressException exc) {
            throw new URISyntaxException(url, exc.getMessage());
        }
    }

    public long create(String key, byte[] value, boolean lease) throws OffkvException {
        return backend.create(handle, key, value, lease);
    }

    public long create(String key, byte[] value) throws OffkvException {
        return create(key, value, false);
    }

    public ExistsResult exists(String key, boolean watch) throws OffkvException {
        return new ExistsResult(backend.exists(handle, key, watch));
    }

    public ExistsResult exists(String key) throws OffkvException {
        return exists(key, false);
    }

    public ChildrenResult getChildren(String key, boolean watch) throws OffkvException {
        return new ChildrenResult(backend.getChildren(handle, key, watch));
    }

    public ChildrenResult getChildren(String key) throws OffkvException {
        return getChildren(key, false);
    }

    public long set(String key, byte[] value) throws OffkvException {
        return backend.set(handle, key, value);
    }

    public GetResult get(String key, boolean watch) throws OffkvException {
        return new GetResult(backend.get(handle, key, watch));
    }

    public GetResult get(String key) throws OffkvException {
        return get(key, false);
    }

    public long compareAndSet(String key, byte[] value, long version) throws OffkvException {
        return backend.compareAndSet(handle, key, value, version);
    }

    public void delete(String key, long version) throws OffkvException {
        backend.delete(handle, key, version);
    }

    public void delete(String key) throws OffkvException {
        delete(key, 0);
    }

    // TODO
    //    virtual TransactionResult commit(const Transaction&) = 0;

    public void close() throws OffkvException {
        backend.free(handle);
    }
}
