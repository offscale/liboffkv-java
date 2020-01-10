package io.offscale.liboffkv;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URISyntaxException;

@RunWith(Parameterized.class)
public class OpenCloseTest {
    private final String address;

    public OpenCloseTest(String addr) {
        address = addr;
    }

    @Test
    public void createDestroy() throws OffkvException, URISyntaxException {
        try (OffkvClient client = new OffkvClient(address, "")) {}
    }

    @Test
    public void createDestroy2() throws OffkvException, URISyntaxException {
        try (OffkvClient client = new OffkvClient(address, "/test/prefixed")) {}
    }

    @Test(expected = InvalidKeyException.class)
    public void wrongPrefix() throws OffkvException, URISyntaxException {
        try (OffkvClient client = new OffkvClient(address, "dosmth")) {}
    }

    @Test(expected = URISyntaxException.class)
    public void wrongAddress() throws OffkvException, URISyntaxException {
        try (OffkvClient client = new OffkvClient("wrong://127.0.0.1:2222", "")) {}
    }

    @Parameterized.Parameters
    public static Iterable<String> serviceAddresses() {
        return OffkvTestBase.serviceAddresses();
    }
}
