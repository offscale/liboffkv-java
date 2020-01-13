package io.offscale.liboffkv;

public class WatchableResult implements AutoCloseable {
    private final NativeClient backend;
    private long watchHandle;

    protected WatchableResult(NativeClient backend, long watchHandle) {
        this.backend = backend;
        this.watchHandle = watchHandle;
    }

    public void waitChanges() throws OffkvException {
        if (!isWatchable())
            throw new IllegalStateException("Result is not watchable");

        try {
            backend.waitChanges(watchHandle);
        } finally {
            close();
        }
    }

    public boolean isWatchable() {
        return watchHandle != 0;
    }

    @Override
    public void close() {
        if (isWatchable()) {
            backend.freeWatch(watchHandle);
            watchHandle = 0;
        }
    }
}
