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

    public native long create(long handle, String key, byte[] value, boolean lease)
            throws OffkvException;
    public native ResultHandle<Void> exists(long handle, String key, boolean watch)
            throws OffkvException;
    public native ResultHandle<byte[]> get(long handle, String key, boolean watch)
            throws OffkvException;
    public native ResultHandle<String[]> getChildren(long handle, String key, boolean watch)
            throws OffkvException;
    public native long compareAndSet(long handle, String key, byte[] value, long version)
            throws OffkvException;
    public native long set(long handle, String key, byte[] value)
            throws OffkvException;
    public native void delete(long handle, String key, long version)
            throws OffkvException;
    public native long[] commit(long handle, TransactionCheck[] checks, TransactionOperation[] operations)
            throws OffkvException, TransactionFailedException;

    public native long connect(String url, String prefix)
            throws OffkvException, InvalidAddressException;
    public native void waitChanges(long watchHandle)
            throws OffkvException;
    public native void free(long handle);
    public native void freeWatch(long watchHandle);
}
