package org.phantazm.server;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorFeature {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorFeature.class);
    private static final Field TARGET_FIELD;

    static {
        try {
            TARGET_FIELD = Thread.class.getDeclaredField("target");
            TARGET_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

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

                Path logFile = Path.of("./logs/thread_dump_" + Instant.now());
                LOGGER.warn("Writing thread dump to log file " + logFile);

                Map<Thread, StackTraceElement[]> dump = Thread.getAllStackTraces();
                StringBuilder dumpText = new StringBuilder();
                for (Thread thread : dump.keySet()) {
                    String name = thread.getName();
                    ThreadGroup group = thread.getThreadGroup();
                    Thread.State state = thread.getState();
                    int priority = thread.getPriority();
                    boolean daemon = thread.isDaemon();

                    dumpText.append("Thread ").append(name).append(" (").append(state).append(")").append(':')
                        .append(System.lineSeparator());
                    dumpText.append("in group ").append(group.getName()).append(System.lineSeparator());
                    dumpText.append("priority ").append(priority).append(System.lineSeparator());
                    dumpText.append("daemon: ").append(daemon).append(System.lineSeparator());

                    try {
                        Runnable value = (Runnable) TARGET_FIELD.get(thread);
                        dumpText.append("target runnable: ").append(value == null ? "null" : value.getClass())
                            .append(System.lineSeparator());
                    } catch (IllegalAccessException ignored) {
                        dumpText.append("[failed to read target runnable field]");
                    }

                    dumpText.append("=====================").append(System.lineSeparator()).append(System.lineSeparator());
                    for (StackTraceElement stackTraceElement : dump.get(thread)) {
                        dumpText.append(stackTraceElement).append(System.lineSeparator());
                    }

                    dumpText.append(System.lineSeparator()).append(System.lineSeparator());
                }

                try {
                    FileUtils.ensureDirectories(Path.of("./logs"));
                    Files.createFile(logFile);

                    Files.writeString(logFile, dumpText, StandardOpenOption.WRITE);
                } catch (IOException | UncheckedIOException ignored) {
                    LOGGER.warn("Failed to dump log file. Printing to console instead.");
                    LOGGER.warn(dumpText.toString());
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
