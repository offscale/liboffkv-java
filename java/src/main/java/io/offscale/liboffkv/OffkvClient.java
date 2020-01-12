package io.offscale.liboffkv;

import java.net.URISyntaxException;
import java.util.Objects;

public class OffkvClient implements AutoCloseable {
    private final NativeClient backend = NativeClient.getInstance();
    private long handle;

    public OffkvClient(String url, String prefix) throws URISyntaxException, OffkvException {
        try {
            handle = backend.connect(Objects.requireNonNull(url), Objects.requireNonNull(prefix));
        } catch (InvalidAddressException exc) {
            throw new URISyntaxException(url, exc.getMessage());
        }
    }

    public long create(String key, byte[] value, boolean lease) throws OffkvException {
        return backend.create(handle, Objects.requireNonNull(key), Objects.requireNonNull(value), lease);
    }

    public long create(String key, byte[] value) throws OffkvException {
        return create(key, value, false);
    }

    public ExistsResult exists(String key, boolean watch) throws OffkvException {
        return new ExistsResult(backend.exists(handle, Objects.requireNonNull(key), watch));
    }

    public ExistsResult exists(String key) throws OffkvException {
        return exists(key, false);
    }

    public ChildrenResult getChildren(String key, boolean watch) throws OffkvException {
        return new ChildrenResult(backend.getChildren(handle, Objects.requireNonNull(key), watch));
    }

    public ChildrenResult getChildren(String key) throws OffkvException {
        return getChildren(key, false);
    }

    public long set(String key, byte[] value) throws OffkvException {
        return backend.set(handle, Objects.requireNonNull(key), Objects.requireNonNull(value));
    }

    public GetResult get(String key, boolean watch) throws OffkvException {
        return new GetResult(backend.get(handle, Objects.requireNonNull(key), watch));
    }

    public GetResult get(String key) throws OffkvException {
        return get(key, false);
    }

    public long compareAndSet(String key, byte[] value, long version) throws OffkvException {
        return backend.compareAndSet(handle, Objects.requireNonNull(key), Objects.requireNonNull(value), version);
    }

    public void delete(String key, long version) throws OffkvException {
        checkState();
        backend.delete(handle, Objects.requireNonNull(key), version);
    }

    public void delete(String key) throws OffkvException {
        delete(key, 0);
    }

    public TransactionBuilder transaction() {
        return new TransactionBuilder(() -> {
            checkState();
            return handle;
        });
    }

    public void close() {
        backend.free(handle);
        handle = 0;
    }

    private void checkState() {
        if (handle == 0)
            throw new IllegalStateException("Client is closed");
    }
}
