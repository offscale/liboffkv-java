package io.offscale.liboffkv;

class TransactionCheck {
    String key;
    long version;

    public TransactionCheck(String key, long version) {
        this.key = key;
        this.version = version;
    }
}
