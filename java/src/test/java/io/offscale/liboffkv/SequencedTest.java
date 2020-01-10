package io.offscale.liboffkv;

import io.offscale.liboffkv.test.CheckpointBarriers;
import io.offscale.liboffkv.test.Ensure;
import io.offscale.liboffkv.test.ThrowingConsumer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RunWith(Parameterized.class)
public class SequencedTest extends OffkvTestBase {
    private final List<CompletableFuture<Void>> futures = Collections.synchronizedList(new ArrayList<>());
    private final List<Thread> threads = Collections.synchronizedList(new ArrayList<>());

    public SequencedTest(String addr) {
        super(addr);
    }

    @Test
    public void existsWatch() throws ExecutionException {
        use("/key");
        CheckpointBarriers<Integer> flow = new CheckpointBarriers<>(2);
        byte[] val = getSomeData();

        thread(c -> {
            c.create("/key", val);
            ExistsResult result = c.exists("/key", true);

            flow.point(1);

            Assert.assertTrue(result.exists());
            result.waitChanges();
            Assert.assertFalse(c.exists("/key").exists());
        });

        thread(c -> {
            flow.point(1);

            c.delete("/key");
        });

        jointAll();
    }

    @Test
    public void existsWatchUseful() throws ExecutionException {
        use("/key");
        CheckpointBarriers<Integer> flow = new CheckpointBarriers<>(2);
        byte[] val = getSomeData();

        thread(c -> {
            c.create("/key", val);
            ExistsResult exists = c.exists("/key", true);

            flow.point(1);

            exists.waitChanges();

            flow.point(2);
        });

        thread(c -> {
            flow.point(1);

            Thread.sleep(10000);
            Assert.assertEquals(0, flow.reached(2));
            c.delete("/key");

            flow.point(2);
        });

        jointAll();
    }

    @Test
    public void getWatch() throws ExecutionException {
        use("/key");
        CheckpointBarriers<Integer> flow = new CheckpointBarriers<>(2);
        byte[] val = getSomeData();
        byte[] val2 = getSomeData();

        thread((c) -> {
            c.create("/key", val);
            GetResult result = c.get("/key", true);

            flow.point(1);

            Assert.assertArrayEquals(val, result.getValue());
            result.waitChanges();
            Assert.assertArrayEquals(val2, c.get("/key").getValue());
        });

        thread((c) -> {
            flow.point(1);

            c.set("/key", val2);
        });

        jointAll();
    }

    @Test
    public void getWatchUseful() throws ExecutionException {
        use("/key");
        CheckpointBarriers<Integer> flow = new CheckpointBarriers<>(2);
        byte[] val = getSomeData();
        byte[] val2 = getSomeData();

        thread((c) -> {
            c.create("/key", val);
            GetResult result = c.get("/key", true);

            flow.point(1);

            result.waitChanges();

            flow.point(2);
        });

        thread((c) -> {
            flow.point(1);

            Thread.sleep(10000);
            Assert.assertEquals(0, flow.reached(2));
            c.set("/key", val2);

            flow.point(2);
        });

        jointAll();
    }

    @Test
    public void getChildrenWatch() throws ExecutionException {
        use("/key");
        CheckpointBarriers<Integer> flow = new CheckpointBarriers<>(2);
        byte[] val = getSomeData();

        thread(c -> {
            c.create("/key", val);
            c.create("/key/child", val);
            c.create("/key/child/grandchild", val);
            c.create("/key/dimak24", val);

            ChildrenResult result = newClient().getChildren("/key", true);

            flow.point(1);

            Ensure.equalsAsSets(Arrays.asList("/key/child", "/key/dimak24"), result.getChildren());
            result.waitChanges();
            Ensure.equalsAsSets(Collections.singletonList("/key/child"), c.getChildren("/key").getChildren());
        });

        thread(c -> {
            flow.point(1);

            c.delete("/key/dimak24");
        });

        jointAll();
    }

    // TODO add getChildrenWatchUseful

    @Test
    public void createLeased() throws ExecutionException {
        use("/key");
        CheckpointBarriers<Integer> flow = new CheckpointBarriers<>(2);
        byte[] val = getSomeData();

        thread(c -> {
            c.create("/key", val, true);

            flow.point(1);
            flow.point(2);

            releaseClient(c);

            flow.point(3);
        });

        thread(c -> {
            flow.point(1);

            Assert.assertTrue(c.exists("/key").exists());

            flow.point(2);
            flow.point(3);

            Thread.sleep(25000);

            Assert.assertFalse(c.exists("/key").exists());
        });

        jointAll();
    }

    @After
    public void cleanThreads() {
        threads.clear();
        futures.clear();
    }

    private void jointAll() throws ExecutionException {
        for (CompletableFuture<Void> f : futures) {
            try {
                f.get();
            } catch (InterruptedException | CancellationException e) {
            } catch (ExecutionException exc) {
                if (!(exc.getCause() instanceof InterruptedException))
                    throw exc;
            }
        }
    }

    private void thread(ThrowingConsumer<OffkvClient> r) {
        CompletableFuture<Void> f = new CompletableFuture<>();
        Thread t = new Thread(() -> {
            try {
                r.accept(newClient());
                f.complete(null);
            } catch (Throwable exc) {
                f.completeExceptionally(exc);
                interruptThreads();
            }
        });
        futures.add(f);
        threads.add(t);
        t.start();
    }

    private void interruptThreads() {
        for (Thread t : threads) {
            t.interrupt();
        }
    }

    @Parameterized.Parameters
    public static Iterable<String> serviceAddresses() {
        return OffkvTestBase.serviceAddresses();
    }
}
