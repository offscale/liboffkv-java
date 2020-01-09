package io.offscale.liboffkv;

public class NoChildrenForEphemeralException extends OffkvException {
    public NoChildrenForEphemeralException() {
        super("Children are not allowed for leased nodes");
    }
}
