package com.github.phantazmnetwork.zombies.equipment;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

public interface Upgradable {

    @NotNull Set<Key> getSuggestedUpgrades();

    @NotNull Collection<Key> getLevels();

    void setLevel(@NotNull Key key);

}
