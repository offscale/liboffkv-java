package io.offscale.liboffkv;

public class GetResult extends WatchableResult {
    private long version;
    private byte[] value;

    GetResult(long version, byte[] value, long watchHandle) {
        super(watchHandle);
        this.version = version;
        this.value = value;
    }

    GetResult(ResultHandle<byte[]> nativeResult) {
        this(nativeResult.version, nativeResult.value, nativeResult.watch);
    }

    public long getVersion() {
        return version;
    }

    public byte[] getValue() {
        return value;
    }
}
