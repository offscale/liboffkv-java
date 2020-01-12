package io.offscale.liboffkv;

class TransactionFailedException extends Exception {
    private int failedIndex;

    public TransactionFailedException(int failedIndex) {
        this.failedIndex = failedIndex;
    }

    public int getFailedIndex() {
        return failedIndex;
    }
}
