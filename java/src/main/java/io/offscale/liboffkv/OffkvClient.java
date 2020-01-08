package io.offscale.liboffkv;

import java.io.Closeable;

public class OffkvClient implements Closeable {
    private final NativeClient backend = new NativeClient();

    public OffkvClient(String url, String prefix) {
        backend.connect(url, prefix);
    }

    public void close() throws OffkvException {
        backend.free();
    }
}
