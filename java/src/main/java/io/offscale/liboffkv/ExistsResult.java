package io.offscale.liboffkv;

public class ExistsResult extends WatchableResult {
    private long version;

    private ExistsResult(NativeClient backend, long version, long watchHandle) {
        super(backend, watchHandle);
        this.version = version;
    }

    ExistsResult(NativeClient backend, ResultHandle<Void> nativeResult) {
        this(backend, nativeResult.version, nativeResult.watch);
    }

    public boolean exists() {
        return version != 0;
    }

    public long getVersion() {
        return version;
    }
}
