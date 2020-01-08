package io.offscale.liboffkv;

class NativeClient {
    static {
        System.loadLibrary("offkv_java_bindings");
    }

    public native void connect(String url, String prefix);
    public native void free();
}
