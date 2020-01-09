package io.offscale.liboffkv;

public class ServiceException extends OffkvException {
    public ServiceException() {
        super("Unknown service exception");
    }

    public ServiceException(String message) {
        super(message);
    }
}
