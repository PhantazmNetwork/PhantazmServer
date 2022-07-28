package com.github.phantazmnetwork.zombies.game.player.state;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ZombiesPlayerState {

    void start();

    @NotNull Optional<ZombiesPlayerState> tick(long time);

    void end();

    @NotNull Component getDisplayName();

}
