package com.github.phantazmnetwork.zombies.game.player.state;

import com.github.phantazmnetwork.core.player.PlayerView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class AlivePlayerState implements ZombiesPlayerState {

    private final PlayerView playerView;

    private final Consumer<? super Player> aliveAction;

    public AlivePlayerState(@NotNull PlayerView playerView, @NotNull Consumer<? super Player> aliveAction) {
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.aliveAction = Objects.requireNonNull(aliveAction, "aliveAction");
    }

    @Override
    public void start() {
        playerView.getPlayer().ifPresent(aliveAction);
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
        return Component.text("ALIVE", NamedTextColor.GREEN);
    }
}
