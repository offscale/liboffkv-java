package io.offscale.liboffkv.test;

public interface ThrowingConsumer<T> {
    void accept(T value) throws Exception;
}
