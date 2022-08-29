package com.github.phantazmnetwork.zombies.equipment.upgrade;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface UpgradeNodeData {
    @NotNull Key levelKey();

    @NotNull Set<Key> upgrades();
}
