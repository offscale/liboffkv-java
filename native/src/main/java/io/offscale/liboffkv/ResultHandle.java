package io.offscale.liboffkv;

public class ResultHandle<ResType> {
    public long version;
    public ResType value;
    public long watch;

    public ResultHandle(long version, ResType value, long watch) {
        this.version = version;
        this.value = value;
        this.watch = watch;
    }
}
