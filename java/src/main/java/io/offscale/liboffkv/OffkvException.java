package io.offscale.liboffkv;

import java.io.IOException;

public class OffkvException extends IOException {
    public OffkvException() {
    }

    public OffkvException(String message) {
        super(message);
    }
}
