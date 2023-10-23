package org.phantazm.core.chat;

import it.unimi.dsi.fastutil.objects.ObjectBooleanPair;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Represents a chat channel. This is backed by an {@link Audience}, where messages are sent to.
 */
public interface ChatChannel {

    @NotNull CompletableFuture<Void> sendMessage(@NotNull Player from, @NotNull String message,
        @NotNull Consumer<ObjectBooleanPair<Component>> onFailure);

}
