package io.offscale.liboffkv;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChildrenResult extends WatchableResult {
    private List<String> children;

    ChildrenResult(String[] children, long watchHandle) {
        super(watchHandle);
        this.children = Collections.unmodifiableList(Arrays.asList(children));
    }

    ChildrenResult(ResultHandle<String[]> nativeResult) {
        this(nativeResult.value, nativeResult.watch);
    }

    public List<String> getChildren() {
        return children;
    }
}
