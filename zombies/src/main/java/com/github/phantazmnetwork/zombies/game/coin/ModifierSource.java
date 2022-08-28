package com.github.phantazmnetwork.zombies.game.coin;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface ModifierSource {
    @NotNull Collection<Transaction.Modifier> modifiers(@NotNull Key key);
}
