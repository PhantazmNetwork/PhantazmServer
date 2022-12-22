package org.phantazm.zombies.upgrade;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

@FunctionalInterface
public interface UpgradeNode {
    @Unmodifiable @NotNull Set<Key> upgrades();
}
