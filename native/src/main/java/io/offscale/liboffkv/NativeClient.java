package io.offscale.liboffkv;

import java.io.IOException;

class NativeClient {
    private static class LazyHolder {

        static final NativeClient INSTANCE = new NativeClient();

    }
    public static NativeClient get() {
        return LazyHolder.INSTANCE;
    }
    static {
        try {
            LibraryLoader.load("offkv_java_bindings");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private NativeClient() {}

    public native long connect(String url, String prefix);
    public native long create(long handle, String key, byte[] value, boolean lease);
    public native void delete(String key, long version);
    public native void free(long handle);
}
