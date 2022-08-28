package com.github.phantazmnetwork.zombies.game.map.shop;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public interface UpgradePath {
    Key nextUpgrade(@NotNull Key key);
}
