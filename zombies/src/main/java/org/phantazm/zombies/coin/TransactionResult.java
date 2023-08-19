package org.phantazm.zombies.coin;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public record TransactionResult(@NotNull List<Component> displays,
    int change) {

    public TransactionResult {
        Objects.requireNonNull(displays);
    }

    public boolean applyIfAffordable(@NotNull PlayerCoins coins) {
        Objects.requireNonNull(coins);
        boolean canAfford = coins.getCoins() + change >= 0;
        if (canAfford) {
            coins.applyTransaction(this);
            return true;
        }

        return false;
    }

    public boolean isAffordable(@NotNull PlayerCoins coins) {
        Objects.requireNonNull(coins);
        return coins.getCoins() + change >= 0;
    }

    public boolean hasChange() {
        return change != 0;
    }

}
