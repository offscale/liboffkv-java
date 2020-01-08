package io.offscale.liboffkv;

public class WatchableResult {
    private final NativeWatch watch;

    public WatchableResult(NativeWatch watchBackend) {
        this.watch = watchBackend;
    }

    public void waitChanges() {
        if (!isWatchable())
            throw new IllegalStateException("Result is not watchable");

        watch.waitChanges();
    }

    public boolean isWatchable() {
        return watch != null;
    }
}
