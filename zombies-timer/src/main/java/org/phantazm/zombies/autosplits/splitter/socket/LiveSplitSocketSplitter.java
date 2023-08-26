package org.phantazm.zombies.autosplits.splitter.socket;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.autosplits.splitter.AutoSplitSplitter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class LiveSplitSocketSplitter implements AutoSplitSplitter {

    private final Executor executor;

    private final String host;

    private final int port;

    public LiveSplitSocketSplitter(@NotNull Executor executor, @NotNull String host, int port) {
        this.executor = Objects.requireNonNull(executor);
        this.host = Objects.requireNonNull(host);
        this.port = port;
    }

    @Override
    public @NotNull CompletableFuture<Void> startOrSplit() {
        return sendCommand("startorsplit");
    }

    @Override
    public void cancel() {

    }

    @SuppressWarnings("SameParameterValue")
    private CompletableFuture<Void> sendCommand(String command) {
        return CompletableFuture.runAsync(() -> {
            try (Socket socket = new Socket(host, port);
                 OutputStream outputStream = socket.getOutputStream();
                 Writer writer = new OutputStreamWriter(outputStream)) {
                writer.write(command + "\r\n");
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

}
