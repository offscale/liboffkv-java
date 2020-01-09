package io.offscale.liboffkv;

public class KeyAlreadyExistsException extends OffkvException {
    public KeyAlreadyExistsException() {
        super("Key already exists");
    }

    public KeyAlreadyExistsException(String message) {
        super(message);
    }
}
