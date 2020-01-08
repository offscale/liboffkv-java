package io.offscale.liboffkv;

class NativeClient {
    static {
        System.loadLibrary("offkv_java_buindings");
    }

    public native void connect(String url, String prefix);
    public native void free();
}
