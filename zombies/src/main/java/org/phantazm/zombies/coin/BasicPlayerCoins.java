package org.phantazm.zombies.coin;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.coin.component.TransactionComponentCreator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class BasicPlayerCoins implements PlayerCoins {

    private final PlayerView playerView;

    private final TransactionComponentCreator componentCreator;

    private volatile int coins;

    private final Object sync;

    public BasicPlayerCoins(@NotNull PlayerView playerView, @NotNull TransactionComponentCreator componentCreator,
            int initialCoins) {
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.componentCreator = Objects.requireNonNull(componentCreator, "componentCreator");
        this.coins = initialCoins;

        this.sync = new Object();
    }

    @Override
    public @NotNull TransactionResult runTransaction(@NotNull Transaction transaction) {
        synchronized (sync) {
            List<Transaction.Modifier> modifiers = new ArrayList<>(transaction.modifiers());
            modifiers.sort(Comparator.comparingInt(Transaction.Modifier::getPriority).reversed());

            List<Component> modifierNames = new ArrayList<>(modifiers.size());
            int change = transaction.initialChange();
            for (Transaction.Modifier modifier : modifiers) {
                int newChange = modifier.modify(change);
                if (change != newChange) {
                    modifierNames.add(modifier.getDisplayName());
                }

                change = newChange;
            }

            int newCoins = coins + change;
            if (change < 0 && newCoins > coins) {
                change = -(coins - Integer.MIN_VALUE);
            }
            else if (change > 0 && newCoins < coins) {
                change = Integer.MAX_VALUE - coins;
            }

            return new TransactionResult(modifierNames, change);
        }
    }

    @Override
    public int getCoins() {
        return coins;
    }

    @Override
    public void applyTransaction(@NotNull TransactionResult result) {
        synchronized (sync) {
            if (!result.hasChange()) {
                return;
            }

            coins += result.change();
            playerView.getPlayer().ifPresent(player -> {
                Component message =
                        componentCreator.createTransactionComponent(result.modifierNames(), result.change());
                player.sendMessage(message);
            });
        }
    }
}
