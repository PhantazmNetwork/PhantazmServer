package com.github.phantazmnetwork.zombies.game;

import com.github.phantazmnetwork.zombies.game.map.Round;
import org.jetbrains.annotations.NotNull;

public interface RoundAction {
    void perform(@NotNull Round round);

    int priority();
}
