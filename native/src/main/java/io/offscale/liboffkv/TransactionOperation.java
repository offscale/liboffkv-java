package io.offscale.liboffkv;

class TransactionOperation {
    int kind;
    String key;
    byte[] value;
    boolean leased;

    public TransactionOperation(Kind kind, String key, byte[] value, boolean leased) {
        this.kind = kind.id;
        this.key = key;
        this.value = value;
        this.leased = leased;
    }

    enum Kind {
        SET(0), CREATE(1), DELETE(2);

        final int id;

        Kind(int id) {
            this.id = id;
        }
    }
}
