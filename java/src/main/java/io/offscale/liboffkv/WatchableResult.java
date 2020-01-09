package io.offscale.liboffkv;

public class WatchableResult implements AutoCloseable {
    private final NativeClient backend = NativeClient.getInstance();
    private final long watchHandle;

    protected WatchableResult(long watchHandle) {
        this.watchHandle = watchHandle;
    }

    public void waitChanges() {
        if (!isWatchable())
            throw new IllegalStateException("Result is not watchable");

        backend.waitChanges(watchHandle);
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
