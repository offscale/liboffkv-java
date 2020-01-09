package io.offscale.liboffkv;

public class ConnectionLostException extends OffkvException {
    public ConnectionLostException() {
        super("Connection lost");
    }
}
