package org.phantazm.zombies.coin;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Tickable;

public interface PlayerCoins extends Tickable {
    @NotNull TransactionResult runTransaction(@NotNull Transaction transaction);

    default @NotNull TransactionResult modify(int change) {
        return runTransaction(new Transaction(change));
    }

    int getCoins();

    void applyTransaction(@NotNull TransactionResult result);

    void set(int newValue);
}
