package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.zombies.game.coin.Transaction;
import com.github.phantazmnetwork.zombies.game.coin.TransactionResult;
import com.github.phantazmnetwork.zombies.game.map.shop.InteractionTypes;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.map.shop.Shop;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class FlaggingInteractor extends TransactionalInteractor {

    @Override
    public @Nullable Pair<Transaction, Consumer<TransactionResult>> getTransactionHook(@NotNull Shop shop,
                                                                                       @NotNull PlayerInteraction interaction) {
        if (interaction.key().equals(InteractionTypes.RIGHT_CLICK)) {

        }

        return null;
    }
}
