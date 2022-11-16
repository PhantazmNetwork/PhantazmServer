package com.github.phantazmnetwork.zombies.map;

import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public record PowerupInfo(@NotNull Key id,
                          @NotNull ConfigList visuals,
                          @NotNull ConfigList actions,
                          @NotNull ConfigNode deactivationPredicate) {
}
