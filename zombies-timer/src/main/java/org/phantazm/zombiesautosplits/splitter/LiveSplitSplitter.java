package org.phantazm.zombiesautosplits.splitter;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface LiveSplitSplitter {

    @NotNull CompletableFuture<Void> startOrSplit();

    void cancel();

}
