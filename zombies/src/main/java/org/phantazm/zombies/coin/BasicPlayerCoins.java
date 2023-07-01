package org.phantazm.zombies.coin;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Tickable;
import org.phantazm.stats.zombies.ZombiesPlayerMapStats;
import org.phantazm.zombies.coin.component.TransactionMessager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class BasicPlayerCoins implements PlayerCoins, Tickable {
    private final ZombiesPlayerMapStats stats;
    private final TransactionMessager transactionMessager;
    private int coins;

    public BasicPlayerCoins(@NotNull ZombiesPlayerMapStats stats, @NotNull TransactionMessager transactionMessager,
            int initialCoins) {
        this.stats = Objects.requireNonNull(stats, "stats");
        this.transactionMessager = Objects.requireNonNull(transactionMessager, "componentCreator");
        this.coins = initialCoins;
    }

    @Override
    public @NotNull TransactionResult runTransaction(@NotNull Transaction transaction) {
        List<Transaction.Modifier> modifiers = new ArrayList<>(transaction.modifiers());
        modifiers.sort(Comparator.comparingInt(Transaction.Modifier::getPriority).reversed());

        List<Component> displays = new ArrayList<>(modifiers.size() + transaction.extraDisplays().size());
        int change = transaction.initialChange();
        for (Transaction.Modifier modifier : modifiers) {
            int newChange = modifier.modify(change);
            if (change != newChange) {
                displays.add(modifier.getDisplayName());
            }

            change = newChange;
        }
        displays.addAll(transaction.extraDisplays());

        int newCoins = coins + change;
        if (change < 0 && newCoins > coins) {
            change = -(coins - Integer.MIN_VALUE);
        }
        else if (change > 0 && newCoins < coins) {
            change = Integer.MAX_VALUE - coins;
        }

        return new TransactionResult(displays, change);
    }

    @Override
    public int getCoins() {
        return coins;
    }

    @Override
    public void applyTransaction(@NotNull TransactionResult result) {
        if (!result.hasChange()) {
            return;
        }

        if (result.change() > 0) {
            stats.setCoinsGained(stats.getCoinsGained() + result.change());
        }
        else {
            stats.setCoinsSpent(stats.getCoinsSpent() - result.change());
        }

        coins += result.change();
        transactionMessager.sendMessage(result.displays(), result.change());
    }

    @Override
    public void set(int newValue) {
        coins = newValue;
    }

    @Override
    public void tick(long time) {
        transactionMessager.tick(time);
    }
}
