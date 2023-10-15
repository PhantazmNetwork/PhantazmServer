package org.phantazm.zombies.autosplits.splitter.internal;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.FutureUtils;
import org.phantazm.zombies.autosplits.splitter.AutoSplitSplitter;

import java.util.concurrent.CompletableFuture;

public class InternalSplitter implements AutoSplitSplitter {

    private long splitTime = 0L;

    @Override
    public @NotNull CompletableFuture<Void> startOrSplit() {
        splitTime = System.currentTimeMillis();
        return FutureUtils.nullCompletedFuture();
    }

    public void cancel() {
        splitTime = 0L;
    }

    public long getMillis() {
        if (splitTime == 0L) {
            return 0L;
        }

        return System.currentTimeMillis() - splitTime;
    }
}
