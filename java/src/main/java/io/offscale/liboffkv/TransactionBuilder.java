package io.offscale.liboffkv;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class TransactionBuilder {
    private final NativeClient backend = NativeClient.getInstance();
    private final List<TransactionCheck> checks = new ArrayList<>();
    private final List<TransactionOperation> operations = new ArrayList<>();
    private final Supplier<Long> handleSupplier;
    private boolean moreChecksExpected = true;

    TransactionBuilder(Supplier<Long> handleSupplier) {
        this.handleSupplier = handleSupplier;
    }

    public TransactionBuilder check(String key, long version) {
        if (!moreChecksExpected)
            throw new IllegalStateException("Checks must precede operations");

        checks.add(new TransactionCheck(Objects.requireNonNull(key), version));
        return this;
    }

    public TransactionBuilder create(String key, byte[] value, boolean leased) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        startOperationsSection();

        operations.add(new TransactionOperation(
                TransactionOperation.Kind.CREATE, key, value, leased
        ));
        return this;
    }

    public TransactionBuilder create(String key, byte[] value) {
        return create(key, value, false);
    }

    public TransactionBuilder set(String key, byte[] value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        startOperationsSection();

        operations.add(new TransactionOperation(
                TransactionOperation.Kind.SET, key, value, false
        ));
        return this;
    }

    public TransactionBuilder delete(String key) {
        Objects.requireNonNull(key);
        startOperationsSection();

        operations.add(new TransactionOperation(
                TransactionOperation.Kind.DELETE, key, null, false
        ));
        return this;
    }

    public TransactionResult commit() throws OffkvException {
        try {
            long[] results = backend.commit(
                    handleSupplier.get(),
                    checks.toArray(new TransactionCheck[0]),
                    operations.toArray(new TransactionOperation[0])
            );
            TransactionResult.OperationResult[] opResults = new TransactionResult.OperationResult[results.length];
            int index = 0;

            for (TransactionOperation op : operations) {
                if (op.kind == TransactionOperation.Kind.SET.id) {
                    opResults[index] =
                            new TransactionResult.OperationResult(TransactionResult.OperationKind.SET, results[index]);
                    index++;
                } else if (op.kind == TransactionOperation.Kind.CREATE.id) {
                    opResults[index] =
                            new TransactionResult.OperationResult(TransactionResult.OperationKind.CREATE, results[index]);
                    index++;
                }
            }

            return TransactionResult.createSucceded(opResults);
        } catch (TransactionFailedException e) {
            return TransactionResult.createFailed(e.getFailedIndex());
        }
    }

    private void startOperationsSection() {
        moreChecksExpected = false;
    }
}
