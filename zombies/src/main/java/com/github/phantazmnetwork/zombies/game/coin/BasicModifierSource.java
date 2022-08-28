package com.github.phantazmnetwork.zombies.game.coin;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BasicModifierSource implements ModifierSource {
    private final Map<Key, Collection<Transaction.Modifier>> modifierSources;

    public BasicModifierSource() {
        this.modifierSources = new HashMap<>();
    }

    @Override
    public @NotNull Collection<Transaction.Modifier> modifiers(@NotNull Key key) {
        Objects.requireNonNull(key, "key");

        Collection<Transaction.Modifier> modifiers = modifierSources.get(key);
        if (modifiers == null) {
            return Collections.emptyList();
        }

        return modifiers;
    }
}
