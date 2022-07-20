package com.github.phantazmnetwork.zombies.game.coin;

import org.jetbrains.annotations.NotNull;

public interface PlayerCoins {

    @NotNull TransactionResult runTransaction(@NotNull Transaction transaction);

    int getCoins();

    void addCoins(@NotNull TransactionResult result);

}
