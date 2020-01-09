package io.offscale.liboffkv;

import java.io.IOException;

class NativeClient {
    private static class LazyHolder {

        static final NativeClient INSTANCE = new NativeClient();

    }
    public static NativeClient getInstance() {
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
    public native ResultHandle<Void> exists(long handle, String key, boolean watch);
    public native ResultHandle<byte[]> get(long handle, String key, boolean watch);
    public native ResultHandle<String[]> getChildren(long handle, String key, boolean watch);
    public native long compareAndSet(long handle, String key, byte[] value, long version);
    public native long set(long handle, String key, byte[] value);
    public native void delete(long handle, String key, long version);
    public native void free(long handle);
    public native void waitChanges(long watchHandle);
    public native void freeWatch(long watchHandle);
}
