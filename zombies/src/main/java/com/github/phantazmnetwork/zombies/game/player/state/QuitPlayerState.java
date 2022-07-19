package com.github.phantazmnetwork.zombies.game.player.state;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class QuitPlayerState implements ZombiesPlayerState {

    private final Reference<Instance> instanceReference;

    private final CompletableFuture<? extends Component> quitMessageFuture;

    public QuitPlayerState(@NotNull Instance instance,
                           @NotNull CompletableFuture<? extends Component> quitMessageFuture) {
        this.instanceReference = new WeakReference<>(Objects.requireNonNull(instance, "instance"));
        this.quitMessageFuture = Objects.requireNonNull(quitMessageFuture, "quitMessageFuture");
    }

    @Override
    public void start() {
        quitMessageFuture.thenAccept(quitMessage -> {
            Instance instance = instanceReference.get();
            if (instance != null) {
                instance.sendMessage(quitMessage);
            }
        });
    }

    @Override
    public @NotNull Optional<ZombiesPlayerState> tick(long time) {
        return Optional.empty();
    }

    @Override
    public void end() {

    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.text("QUIT", NamedTextColor.RED);
    }
}
