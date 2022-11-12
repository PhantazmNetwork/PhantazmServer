package com.github.phantazmnetwork.zombies.player.state;

import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ZombiesPlayerState extends Keyed {

    void start();

    @NotNull Optional<ZombiesPlayerState> tick(long time);


    void end();

    @NotNull Component getDisplayName();

}
