package io.offscale.liboffkv;

public class GetResult extends WatchableResult {
    private long version;
    private byte[] value;

    GetResult(NativeClient backend, long version, byte[] value, long watchHandle) {
        super(backend, watchHandle);
        this.version = version;
        this.value = value;
    }

    GetResult(NativeClient backend, ResultHandle<byte[]> nativeResult) {
        this(backend, nativeResult.version, nativeResult.value, nativeResult.watch);
    }

    public long getVersion() {
        return version;
    }

    public byte[] getValue() {
        return value;
    }
}
