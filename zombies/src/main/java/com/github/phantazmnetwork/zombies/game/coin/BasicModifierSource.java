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
            throw new IllegalArgumentException("no modifiers present for key '" + key + "'");
        }

        return modifiers;
    }

    @Override
    public void registerType(@NotNull Key key) {
        Objects.requireNonNull(key, "key");
        if (modifierSources.putIfAbsent(key, new ArrayList<>()) != null) {
            throw new IllegalArgumentException("modifierType type '" + key + "' already registered");
        }
    }

    @Override
    public boolean hasType(@NotNull Key key) {
        Objects.requireNonNull(key, "key");
        return modifierSources.containsKey(key);
    }
}
