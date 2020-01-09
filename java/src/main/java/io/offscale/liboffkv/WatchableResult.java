package io.offscale.liboffkv;

public class WatchableResult implements AutoCloseable {
    private final NativeClient backend = NativeClient.getInstance();
    private long watchHandle;

    protected WatchableResult(long watchHandle) {
        this.watchHandle = watchHandle;
    }

    public void waitChanges() throws OffkvException {
        if (!isWatchable())
            throw new IllegalStateException("Result is not watchable");

        try {
            backend.waitChanges(watchHandle);
        } finally {
            close();
            watchHandle = 0;
        }
    }

    public boolean isWatchable() {
        return watchHandle != 0;
    }

    @Override
    public void close() {
        if (isWatchable()) {
            backend.freeWatch(watchHandle);
        }
    }
}
