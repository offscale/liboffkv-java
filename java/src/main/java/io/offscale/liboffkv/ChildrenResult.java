package io.offscale.liboffkv;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChildrenResult extends WatchableResult {
    private List<String> children;

    ChildrenResult(NativeClient backend, String[] children, long watchHandle) {
        super(backend, watchHandle);
        this.children = Collections.unmodifiableList(Arrays.asList(children));
    }

    ChildrenResult(NativeClient backend, ResultHandle<String[]> nativeResult) {
        this(backend, nativeResult.value, nativeResult.watch);
    }

    public List<String> getChildren() {
        return children;
    }
}
