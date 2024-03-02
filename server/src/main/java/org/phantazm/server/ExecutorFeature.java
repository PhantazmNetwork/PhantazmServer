package org.phantazm.server;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorFeature {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorFeature.class);

    private static ExecutorService executorService;

    private ExecutorFeature() {
        executorService.shutdown();
    }

    static void initialize() {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger();

            @Override
            public Thread newThread(@NotNull Runnable r) {
                return new Thread(r, "phantazm-worker-" + counter.getAndIncrement());
            }
        });
    }

    static void shutdown() {
        if (executorService == null) {
            return;
        }

        try {
            LOGGER.info(
                "Shutting down executor. Please allow for one minute before shutdown completes.");
            executorService.shutdown();
            if (!executorService.awaitTermination(1L, TimeUnit.MINUTES)) {
                List<Runnable> runnables = executorService.shutdownNow();

                LOGGER.warn(
                    "Not all tasks completed. Please allow for one minute for tasks to be canceled.");
                LOGGER.warn("Hung tasks: ");
                for (Runnable runnable : runnables) {
                    LOGGER.warn(runnable.getClass().toString());
                }

                if (!executorService.awaitTermination(1L, TimeUnit.MINUTES)) {
                    LOGGER.warn("Database tasks failed to cancel.");
                }
            }
            LOGGER.info("Executor shut down.");
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOGGER.error("Error during executor shutdown", e);
        }
    }

    public static @NotNull Executor getExecutor() {
        return FeatureUtils.check(executorService);
    }
}
