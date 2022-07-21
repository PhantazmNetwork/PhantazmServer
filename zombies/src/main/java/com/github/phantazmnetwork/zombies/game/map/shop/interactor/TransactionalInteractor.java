package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.zombies.game.coin.PlayerCoins;
import com.github.phantazmnetwork.zombies.game.coin.Transaction;
import com.github.phantazmnetwork.zombies.game.coin.TransactionResult;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.map.shop.Shop;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class TransactionalInteractor implements ShopInteractor {
    @Override
    public void tick(long time) {

    }

    @Override
    public final boolean handleInteraction(@NotNull Shop shop, @NotNull PlayerInteraction interaction) {
        Pair<Transaction, Consumer<TransactionResult>> transaction = getTransactionHook(shop, interaction);
        if (transaction == null) {
            return false;
        }

        ZombiesPlayer player = interaction.getPlayer();
        PlayerCoins coins = player.getCoins();
        TransactionResult result = coins.runTransaction(transaction.first());

        if (coins.getCoins() + result.change() > 0) {
            coins.addCoins(result);
            transaction.second().accept(result);
            return true;
        }

        return false;
    }

    public abstract @Nullable Pair<Transaction, Consumer<TransactionResult>> getTransactionHook(@NotNull Shop shop,
                                                                                                @NotNull PlayerInteraction interaction);
}
