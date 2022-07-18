package com.github.phantazmnetwork.zombies.game.coin;

import org.jetbrains.annotations.NotNull;

public interface PlayerCoins {

    void runTransaction(@NotNull Transaction transaction);

    int getCoins();

}
