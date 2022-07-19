package com.github.phantazmnetwork.zombies.game.coin;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public record TransactionResult(@NotNull List<Component> modifierNames, int change) {

    public TransactionResult {
        Objects.requireNonNull(modifierNames, "modifierNames");
    }

}
