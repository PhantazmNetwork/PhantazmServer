package org.phantazm.zombies.upgrade;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Objects;
import java.util.Set;

public abstract class UpgradeNodeBase implements UpgradeNode, Keyed {
    private final UpgradeNodeData data;
    private final Set<Key> unmodifiableUpgrades;

    public UpgradeNodeBase(@NotNull UpgradeNodeData data) {
        this.data = Objects.requireNonNull(data, "data");
        unmodifiableUpgrades = Set.copyOf(data.upgrades());
    }

    @Override
    public final @Unmodifiable @NotNull Set<Key> upgrades() {
        return unmodifiableUpgrades;
    }

    @Override
    public final @NotNull Key key() {
        return data.levelKey();
    }

    protected @NotNull UpgradeNodeData getData() {
        return data;
    }
}