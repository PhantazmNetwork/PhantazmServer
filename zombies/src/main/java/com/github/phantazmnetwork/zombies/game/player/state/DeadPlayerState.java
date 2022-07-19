package com.github.phantazmnetwork.zombies.game.player.state;

import com.github.phantazmnetwork.core.player.PlayerView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class DeadPlayerState implements ZombiesPlayerState {

    private final PlayerView playerView;

    private final Reference<Instance> instanceReference;

    private final CompletableFuture<? extends Component> deathMessageFuture;

    private final Consumer<? super Player> deathAction;

    public DeadPlayerState(@NotNull PlayerView playerView, @NotNull Instance instance,
                           @NotNull CompletableFuture<? extends Component> deathMessageFuture,
                           @NotNull Consumer<? super Player> deathAction) {
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.instanceReference = new WeakReference<>(Objects.requireNonNull(instance, "instance"));
        this.deathMessageFuture = Objects.requireNonNull(deathMessageFuture, "deathMessageFuture");
        this.deathAction = Objects.requireNonNull(deathAction, "deathAction");
    }

    @Override
    public void start() {
        deathMessageFuture.thenAccept(deathMessage -> {
            Instance instance = instanceReference.get();
            if (instance != null) {
                instance.sendMessage(deathMessage);
            }
        });
        playerView.getPlayer().ifPresent(deathAction);
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
        return Component.text("DEAD", NamedTextColor.RED);
    }
}
