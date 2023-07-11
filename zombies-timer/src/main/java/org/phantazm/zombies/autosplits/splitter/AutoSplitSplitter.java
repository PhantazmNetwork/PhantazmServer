package org.phantazm.zombies.autosplits.splitter;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface AutoSplitSplitter {

    @NotNull CompletableFuture<Void> startOrSplit();

    void cancel();

}
