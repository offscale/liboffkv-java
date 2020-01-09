package io.offscale.liboffkv;

public class InvalidKeyException extends OffkvException {
    public InvalidKeyException() {
        super("Key is invalid");
    }

    public InvalidKeyException(String message) {
        super(message);
    }
}
