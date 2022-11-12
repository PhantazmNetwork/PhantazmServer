package com.github.phantazmnetwork.zombies.coin.component;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TransactionComponentCreator {

    @NotNull Component createTransactionComponent(@NotNull List<Component> modifierNames, int change);

}
