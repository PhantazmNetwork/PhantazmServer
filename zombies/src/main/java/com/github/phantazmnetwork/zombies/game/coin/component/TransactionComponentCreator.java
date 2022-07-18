package com.github.phantazmnetwork.zombies.game.coin.component;

import com.github.phantazmnetwork.zombies.game.coin.Transaction;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TransactionComponentCreator {

    @NotNull Component createTransactionComponent(@NotNull List<Transaction.Modifier> modifiers, int change);

}
