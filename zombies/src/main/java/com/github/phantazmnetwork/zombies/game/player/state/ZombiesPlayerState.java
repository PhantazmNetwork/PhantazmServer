package com.github.phantazmnetwork.zombies.game.player.state;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ZombiesPlayerState {

    void start();

    @NotNull Optional<ZombiesPlayerState> tick(long time);

    /**
     * TODO:
     * currently, this will be used to do stuff like announce that a player has been revived or that a quit player
     * has rejoined.
     * however, when the game does cleanup, this method would not make sense to call. in the case that this method
     * does not need to be called, this is OK, but if cleanup behavior is necessary, then we will need a
     * shutdown-like method. look into this
     */
    void end();

    @NotNull Component getDisplayName();

}
