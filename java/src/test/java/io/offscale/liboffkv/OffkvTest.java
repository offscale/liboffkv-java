package io.offscale.liboffkv;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
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
    public void createExists() throws OffkvException {
        Assert.assertFalse(client.exists("/test").exists());
        long v = client.create(use("/test"), getSomeData());
        ExistsResult exists = client.exists("/test");

        Assert.assertTrue(exists.exists());
        Assert.assertEquals(v, exists.getVersion());
    }

    @Test
    public void createGet() throws OffkvException {
        byte[] data = getSomeData();
        long v = client.create(use("/test"), data);
        GetResult get = client.get("/test");

        Assert.assertEquals(v, get.getVersion());
        Assert.assertArrayEquals(data, get.getValue());
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
