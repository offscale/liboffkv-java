package io.offscale.liboffkv;

public class KeyNotFoundException extends OffkvException {
    public KeyNotFoundException() {
        super("Key does not exist");
    }

    public KeyNotFoundException(String message) {
        super(message);
    }
}
