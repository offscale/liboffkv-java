package io.offscale.liboffkv;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

@RunWith(Parameterized.class)
public class OffkvTest {
    private final String address;

    public OffkvTest(String addr) {
        address = addr;
    }

    @Test
    public void createDestroy() throws OffkvException {
        System.out.println(address);
    }

    @Parameterized.Parameters
    public static Iterable<String> serviceAddresses() {
        return Arrays.asList(
                "consul://localhost:8500",
                "zk://localhost:2181",
                "etcd://localhost:2379"
        );
    }
}
