package com.github.phantazmnetwork.zombies.coin;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface ModifierSource {
    @NotNull Collection<Transaction.Modifier> modifiers(@NotNull Key key);

    void addModifier(@NotNull Key group, @NotNull Transaction.Modifier modifier);

    void removeModifier(@NotNull Key group, @NotNull Transaction.Modifier modifier);
}
