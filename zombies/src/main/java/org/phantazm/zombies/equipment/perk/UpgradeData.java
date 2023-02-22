package org.phantazm.zombies.equipment.perk;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@FunctionalInterface
public interface UpgradeData {
    @NotNull Set<Key> upgrades();
}
