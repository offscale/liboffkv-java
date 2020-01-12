package io.offscale.liboffkv;

import io.offscale.liboffkv.test.Ensure;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;

@RunWith(Parameterized.class)
public class BasicTest extends OffkvTestBase {
    private OffkvClient client;

    public BasicTest(String addr) {
        super(addr);
    }

    @Before
    public void initClient() throws URISyntaxException, OffkvException {
        client = newClient();
    }

    @Test
    public void keyValidation() throws OffkvException {
        String[] valids = new String[]{
                "/mykey", "/mykey/child", "/.../.../zookeper"
        };
        String[] invalids = new String[]{
                "", "/",
                "mykey", "/каша", "/test\n", "/test\t",
                "/zookeeper", "/zookeeper/child", "/zookeeper/..", "/one/zookeeper",
                "/one/two//three",
                "/one/two/three/",
                "/one/two/three/.",
                "/one/./three",
                "/one/../three",
                "/one/two/three/.."
        };

        for (String key : invalids) {
            Ensure.threw(() -> client.exists(key), InvalidKeyException.class);
        }

        for (String key : valids) {
            client.exists(key);
        }
    }

    @Test
    public void createExists() throws OffkvException {
        Assert.assertFalse(client.exists("/test").exists());
        long v = client.create(use("/test"), getSomeData());
        ExistsResult exists = client.exists("/test");

        Assert.assertTrue(exists.exists());
        Assert.assertEquals(v, exists.getVersion());
        Ensure.threw(exists::waitChanges, IllegalStateException.class);
    }

    @Test
    public void createGet() throws OffkvException {
        use("/test");
        Ensure.threw(() -> client.get("/test"), KeyNotFoundException.class);

        byte[] data = getSomeData();
        long v = client.create("/test", data);
        GetResult get = client.get("/test");

        Assert.assertEquals(v, get.getVersion());
        Assert.assertArrayEquals(data, get.getValue());
        Ensure.threw(get::waitChanges, IllegalStateException.class);
    }

    @Test
    public void create() throws OffkvException {
        use("/key");

        client.create("/key", getSomeData());
        Ensure.threw(() -> client.create("/key", getSomeData()), KeyAlreadyExistsException.class);

        Ensure.threw(() -> client.create("/key/child/grandchild", getSomeData()), KeyNotFoundException.class);
        client.create("/key/child", getSomeData());
    }

    @Test(expected = KeyNotFoundException.class)
    public void deleteNoKey() throws OffkvException {
        client.delete(use("/key"));
    }

    @Test
    public void deleteExists() throws OffkvException {
        use("/key");

        client.create("/key", getSomeData());
        client.create("/key/child", getSomeData());

        client.delete("/key");
        Assert.assertFalse(client.exists("/key").exists());
        Assert.assertFalse(client.exists("/key/child").exists());
    }

    @Test
    public void versionedDeleteExists() throws OffkvException {
        use("/key");

        long initialVersion = client.create("/key", getSomeData());
        client.delete("/key", initialVersion + 1);
        Assert.assertTrue(client.exists("/key").exists());

        client.delete("/key", initialVersion);
        Assert.assertFalse(client.exists("/key").exists());
    }

    @Test
    public void setGet() throws OffkvException {
        use("/key");
        byte[] val = getSomeData();

        long initialVersion = client.create("/key", getSomeData());
        long version = client.set("/key", val);

        GetResult result = client.get("/key");

        Assert.assertTrue(version > initialVersion);
        Assert.assertArrayEquals(val, result.getValue());
        Assert.assertEquals(version, result.getVersion());
    }

    @Test
    public void hotSetGet() throws OffkvException {
        use("/key");
        byte[] val = getSomeData();

        Ensure.threw(() -> client.set("/key/child", val), KeyNotFoundException.class);
        Ensure.threw(() -> client.set("/key/child/grandchild", val), KeyNotFoundException.class);
        client.set("/key", val);
        Assert.assertArrayEquals(val, client.get("/key").getValue());
    }

    @Test(expected = KeyNotFoundException.class)
    public void getChildrenNoKey() throws OffkvException {
        client.getChildren(use("/key"));
    }

    @Test
    public void getChildren() throws OffkvException {
        use("/key");
        byte[] val = getSomeData();

        client.create("/key", val);
        client.create("/key/child", val);
        client.create("/key/child/grandchild", val);
        client.create("/key/hackerivan", val);

        ChildrenResult result = client.getChildren("/key");
        Ensure.equalsAsSets(Arrays.asList("/key/child", "/key/hackerivan"), result.getChildren());

        result = client.getChildren("/key/child");
        Ensure.equalsAsSets(Collections.singletonList("/key/child/grandchild"), result.getChildren());
        Ensure.threw(result::waitChanges, IllegalStateException.class);
    }

    @Test(expected = KeyNotFoundException.class)
    public void casNoEntry() throws OffkvException {
        client.compareAndSet(use("/key"), getSomeData(), 42);
    }

    @Test
    public void cas() throws OffkvException {
        use("/key");
        byte[] val = getSomeData();
        byte[] val2 = getSomeData();

        long version = client.create("/key", val);

        long casResult = client.compareAndSet("/key", val2, version + 1);
        Assert.assertEquals(0, casResult);

        GetResult get = client.get("/key");
        Assert.assertEquals(version, get.getVersion());
        Assert.assertArrayEquals(val, get.getValue());

        casResult = client.compareAndSet("/key", val2, version);
        Assert.assertTrue(casResult > version);

        get = client.get("/key");
        Assert.assertEquals(casResult, get.getVersion());
        Assert.assertArrayEquals(val2, get.getValue());
    }

    @Test
    public void casZeroVersion() throws OffkvException {
        use("/key");
        byte[] val = getSomeData();
        byte[] val2 = getSomeData();

        long casResult = client.compareAndSet("/key", val, 0);
        Assert.assertTrue(casResult > 0);
        Assert.assertArrayEquals(val, client.get("/key").getValue());

        long casResult2 = client.compareAndSet("/key", val2, 0);
        Assert.assertEquals(0, casResult2);
        Assert.assertArrayEquals(val, client.get("/key").getValue());
    }

    @Test
    public void transactionInterfaceFailures() {
        String key = "/key";
        byte[] val = getSomeData();

        Ensure.threw(() -> client.transaction().set(key, val).check(key, 1), IllegalStateException.class);
        Ensure.threw(() -> client.transaction().create(key, val).check(key, 1), IllegalStateException.class);
        Ensure.threw(() -> client.transaction().delete(key).check(key, 1), IllegalStateException.class);
    }

    @Test
    public void transactionSucceeded() throws OffkvException {
        byte[] val = getSomeData();

        long foo_version = client.create(use("/foo"), val);
        long bar_version = client.create("/foo/bar", val);

        TransactionResult result = client.transaction()
                .check("/foo", foo_version)
                .check("/foo/bar", bar_version)
                .create("/foo/child", val)
                .delete("/foo/bar")
                .commit();

        Assert.assertTrue(result.succeeded());
        Assert.assertEquals(1, result.getOperationResults().size());
        Assert.assertEquals(TransactionResult.OperationKind.CREATE, result.getOperationResults().get(0).kind);

        Assert.assertTrue(client.exists("/foo/child").exists());
        Assert.assertArrayEquals(val, client.get("/foo/child").getValue());
        Assert.assertFalse(client.exists("/foo/bar").exists());
    }

    @Test
    public void transactionCheckFailed() throws OffkvException {
        byte[] val = getSomeData();
        byte[] val2 = getSomeData();

        long key_version = client.create(use("/key"), val);
        long foo_version = client.create(use("/foo"), val);
        long bar_version = client.create("/foo/bar", val);

        TransactionResult result = client.transaction()
                .check("/key", key_version)
                .check("/foo", foo_version + 1)
                .check("/foo/bar", bar_version)
                .create("/key/child", val)
                .set("/key", val2)
                .delete("/foo")
                .commit();

        Assert.assertFalse(result.succeeded());
        Assert.assertEquals(1, result.getFailedIndex());

        Assert.assertFalse(client.exists("/key/child").exists());
        Assert.assertArrayEquals(val, client.get("/key").getValue());
        Assert.assertTrue(client.exists("/foo").exists());
    }

    // TODO: add leased transaction test

    @Test
    public void transactionNpe() {
        TransactionBuilder builder = client.transaction();
        byte[] val = getSomeData();

        ensureNpe(() -> builder.check(null, 1));

        ensureNpe(() -> builder.create(null, val));
        ensureNpe(() -> builder.create("/key", null));

        ensureNpe(() -> builder.set("/key", null));
        ensureNpe(() -> builder.set(null, val));

        ensureNpe(() -> builder.delete(null));
    }

    @Test
    public void npe() {
        use("/key");
        byte[] val = getSomeData();

        ensureNpe(() -> client.set("/key", null));
        ensureNpe(() -> client.set(null, val));

        ensureNpe(() -> client.create("/key", null));
        ensureNpe(() -> client.create(null, val));

        ensureNpe(() -> client.exists(null));
        ensureNpe(() -> client.exists(null, true));

        ensureNpe(() -> client.get(null));
        ensureNpe(() -> client.get(null, true));

        ensureNpe(() -> client.getChildren(null));
        ensureNpe(() -> client.getChildren(null, true));

        ensureNpe(() -> client.delete(null));

        ensureNpe(() -> client.compareAndSet(null, val, 1));
        ensureNpe(() -> client.compareAndSet("/key", null, 1));
    }

    private static void ensureNpe(Ensure.Func func) {
        Ensure.threw(func, NullPointerException.class);
    }


    @Parameterized.Parameters
    public static Iterable<String> serviceAddresses() {
        return OffkvTestBase.serviceAddresses();
    }
}
