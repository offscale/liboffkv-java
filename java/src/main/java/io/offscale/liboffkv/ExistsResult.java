package io.offscale.liboffkv;

public class ExistsResult extends WatchableResult {
    private long version;

    private ExistsResult(long version, long watchHandle) {
        super(watchHandle);
        this.version = version;
    }

    ExistsResult(ResultHandle<Void> nativeResult) {
        this(nativeResult.version, nativeResult.watch);
    }

    public boolean exists() {
        return version != 0;
    }

    public long getVersion() {
        return version;
    }
}
