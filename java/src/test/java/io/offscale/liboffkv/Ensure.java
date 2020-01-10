package io.offscale.liboffkv;

import junit.framework.AssertionFailedError;
import org.junit.Assert;

import java.util.Collection;
import java.util.HashSet;

public class Ensure {
    private Ensure() {}

    public static void threw(Func func, Class<? extends Throwable> exceptionClass) {
        String exceptionName = exceptionClass.getName();
        try {
            func.run();
        } catch (AssertionError exc) {
            throw exc;
        } catch (Exception exc) {
            if (exceptionClass.isInstance(exc)) {
                return;
            } else {
                exc.printStackTrace();
                String message = String.format(
                        "func is expected to throw %s but it threw %s", exceptionName, exc.getClass().getName());
                throw new AssertionFailedError(message);
            }
        }
        Assert.fail(String.format("func is expected to throw %s but it did not", exceptionName));
    }

    public static <T> void equalsAsSets(Collection<T> expected, Collection<T> got) {
        Assert.assertEquals(new HashSet<>(expected), new HashSet<>(got));
    }

    public interface Func {
        void run() throws Exception;
    }
}
