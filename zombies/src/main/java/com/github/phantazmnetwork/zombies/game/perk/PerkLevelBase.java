package com.github.phantazmnetwork.zombies.game.perk;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Objects;
import java.util.Set;

public abstract class PerkLevelBase implements PerkLevel {
    private final Key levelKey;
    private final Set<Key> upgrades;

    public PerkLevelBase(@NotNull Key levelKey, @NotNull Set<Key> upgrades) {
        this.levelKey = Objects.requireNonNull(levelKey, "levelKey");
        this.upgrades = Set.copyOf(upgrades);
    }

    @Override
    public @Unmodifiable @NotNull Set<Key> upgrades() {
        return upgrades;
    }

    @Override
    public @NotNull Key key() {
        return levelKey;
    }
}
