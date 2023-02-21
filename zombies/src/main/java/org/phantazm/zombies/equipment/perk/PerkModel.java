package org.phantazm.zombies.equipment.perk;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record PerkModel(@NotNull Key rootLevel, @NotNull Map<Key, PerkLevel> levels) {
}
