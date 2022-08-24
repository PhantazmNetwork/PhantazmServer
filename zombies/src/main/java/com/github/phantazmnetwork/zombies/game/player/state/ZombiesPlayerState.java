package com.github.phantazmnetwork.zombies.game.player.state;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface ZombiesPlayerState extends Keyed {

    void start(@Nullable Key previousStateKey);

    @NotNull Optional<ZombiesPlayerState> tick(long time);


    void end(@Nullable Key nextStateKey);

    @NotNull Component getDisplayName();

}
