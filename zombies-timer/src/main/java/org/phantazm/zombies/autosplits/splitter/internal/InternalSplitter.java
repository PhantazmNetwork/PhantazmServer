package org.phantazm.zombies.autosplits.splitter.internal;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.autosplits.splitter.LiveSplitSplitter;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class InternalSplitter implements LiveSplitSplitter {

    private final ReentrantLock lock = new ReentrantLock();

    private final ScheduledExecutorService executor;

    private Future<?> future = null;

    private long millis = 0L;

    public InternalSplitter(@NotNull ScheduledExecutorService executor) {
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    @Override
    public @NotNull CompletableFuture<Void> startOrSplit() {
        lock.lock();
        try {
            if (future == null) {
                future = executor.scheduleAtFixedRate(() -> {
                    lock.lock();
                    try {
                        millis += 10L;
                    }
                    finally {
                        lock.unlock();
                    }
                }, 0L, 10L, TimeUnit.MILLISECONDS);
            }
            else {
                millis = 0L;
            }

            return CompletableFuture.completedFuture(null);
        }
        finally {
            lock.unlock();
        }
    }

    public void cancel() {
        lock.lock();
        try {
            if (future != null) {
                future.cancel(false);
                future = null;
            }
            millis = 0L;
        }
        finally {
            lock.unlock();
        }
    }

    public long getMillis() {
        return millis;
    }
}
