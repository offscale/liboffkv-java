package io.offscale.liboffkv;

import java.io.IOException;

class NativeClient {
    static {
        try {
            LibraryLoader.load("offkv_java_bindings");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized native long create(long handle, String key, byte[] value, boolean lease)
            throws OffkvException;
    public synchronized native ResultHandle<Void> exists(long handle, String key, boolean watch)
            throws OffkvException;
    public synchronized native ResultHandle<byte[]> get(long handle, String key, boolean watch)
            throws OffkvException;
    public synchronized native ResultHandle<String[]> getChildren(long handle, String key, boolean watch)
            throws OffkvException;
    public synchronized native long compareAndSet(long handle, String key, byte[] value, long version)
            throws OffkvException;
    public synchronized native long set(long handle, String key, byte[] value)
            throws OffkvException;
    public synchronized native void delete(long handle, String key, long version)
            throws OffkvException;
    public synchronized native long[] commit(long handle, TransactionCheck[] checks, TransactionOperation[] operations)
            throws OffkvException, TransactionFailedException;

    public synchronized native long connect(String url, String prefix)
            throws OffkvException, InvalidAddressException;
    public native void waitChanges(long watchHandle)
            throws OffkvException;
    public synchronized native void free(long handle);
    public native void freeWatch(long watchHandle);
}
