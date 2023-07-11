package org.phantazm.zombiesautosplits.splitter;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Objects;

public class CompositeSplitter {

    private final MinecraftClient client;

    private final Logger logger;

    private final Collection<LiveSplitSplitter> splitters;

    private boolean enabled = true;

    public CompositeSplitter(@NotNull MinecraftClient client, @NotNull Logger logger, @NotNull Collection<LiveSplitSplitter> splitters) {
        this.client = Objects.requireNonNull(client, "client");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.splitters = Objects.requireNonNull(splitters, "splitters");
    }

    public void split() {
        for (LiveSplitSplitter splitter : splitters) {
            splitter.startOrSplit().whenComplete((ignored, throwable) -> {
                if (throwable == null) {
                    return;
                }

                logger.warn("Failed to split", throwable);
                client.execute(() -> {
                    if (client.player != null) {
                        MutableText message = Text.literal("Failed to split!");
                        message.formatted(Formatting.RED);
                        client.player.sendMessage(message, false);
                    }
                });
            });
        }
    }

    public boolean toggle() {
        return enabled = !enabled;
    }

}
