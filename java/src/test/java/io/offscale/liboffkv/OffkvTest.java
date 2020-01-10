package io.offscale.liboffkv;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Logger;

@RunWith(Parameterized.class)
public class OffkvTest {
    private final Random random = new Random();
    private final Logger logger = Logger.getLogger("test");

    private final String address;
    private final Set<String> usedKeys = new HashSet<>();
    private OffkvClient client;

    public OffkvTest(String addr) {
        address = addr;
        logger.info("Run with address: " + addr);
    }

    @Before
    public void initClient() throws URISyntaxException, OffkvException {
        client = new OffkvClient(address, "/unitTests");
    }

    @After
    public void destroyClient() throws OffkvException {
        for (String key : usedKeys) {
            try {
                client.delete(key);
            } catch (KeyNotFoundException ignored) {
            } catch (OffkvException exc) {
                exc.printStackTrace();
            }
        }
        client.close();
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
    }

    @Test
    public void createTest() throws OffkvException {
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

    private byte[] getSomeData() {
        byte[] data = new byte[128]; // TODO: increase size
        random.nextBytes(data);
        return data;
    }

    private String use(String key) {
        usedKeys.add(key);
        return key;
    }

    @Parameterized.Parameters
    public static Iterable<String> serviceAddresses() {
        return OpenCloseTest.serviceAddresses();
    }
}
