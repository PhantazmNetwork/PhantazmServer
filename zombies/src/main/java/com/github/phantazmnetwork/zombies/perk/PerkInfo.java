package com.github.phantazmnetwork.zombies.perk;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public record PerkInfo(@NotNull Key perkKey, @NotNull Key rootLevel) {
}
