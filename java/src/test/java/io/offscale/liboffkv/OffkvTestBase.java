package io.offscale.liboffkv;

import org.junit.After;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

public class OffkvTestBase {
    protected final Logger logger = Logger.getLogger("test");

    private final Random random = new Random();
    private final Set<String> usedKeys = new HashSet<>();
    private final Set<OffkvClient> clients = new HashSet<>();

    private final String address;

    protected OffkvTestBase(String addr) {
        address = addr;
        logger.info("Run " + getClass().getSimpleName() + " with address: " + addr);
    }

    protected synchronized OffkvClient newClient() throws URISyntaxException, OffkvException {
        OffkvClient client = new OffkvClient(address, "/unitTests/" + getClass().getSimpleName());
        clients.add(client);
        return client;
    }

    protected synchronized void releaseClient(OffkvClient client) {
        client.close();
        clients.remove(client);
    }

    @After
    public synchronized void cleanup() throws URISyntaxException, OffkvException {
        OffkvClient client;
        if (clients.isEmpty())
            client = newClient();
        else
            client = clients.iterator().next();

        for (String key : usedKeys) {
            try {
                client.delete(key);
            } catch (KeyNotFoundException ignored) {
            } catch (OffkvException exc) {
                exc.printStackTrace();
            }
        }

        for (OffkvClient cl : clients) {
            cl.close();
        }
    }

    protected byte[] getSomeData() {
        byte[] data = new byte[128]; // TODO: increase size
        random.nextBytes(data);
        return data;
    }

    protected synchronized String use(String key) {
        usedKeys.add(key);
        return key;
    }

    public static Iterable<String> serviceAddresses() {
        return Arrays.asList(
                "etcd://localhost:2379",
                "zk://localhost:2181",
                "consul://localhost:8500"
        );
    }
}
