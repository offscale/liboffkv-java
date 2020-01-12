package io.offscale.liboffkv;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TransactionResult {
    private List<OperationResult> results;
    private int failedIndex;

    private TransactionResult(int failedIndex) {
        this.failedIndex = failedIndex;
    }

    private TransactionResult(OperationResult[] results) {
        this.results = Collections.unmodifiableList(Arrays.asList(results));
    }

    public boolean succeeded() {
        return results != null;
    }

    public List<OperationResult> getOperationResults() {
        if (!succeeded())
            throw new IllegalStateException("Operation failed");
        return results;
    }

    public int getFailedIndex() {
        if (succeeded())
            throw new IllegalStateException("Operation succeeded");

        return failedIndex;
    }

    public static TransactionResult createFailed(int index) {
        return new TransactionResult(index);
    }

    public static TransactionResult createSucceded(OperationResult[] results) {
         return new TransactionResult(results);
    }

    public static class OperationResult {
        public final OperationKind kind;
        public final long version;

        public OperationResult(OperationKind kind, long version) {
            this.kind = kind;
            this.version = version;
        }
    }

    public enum OperationKind {
        SET, CREATE
    }
}
