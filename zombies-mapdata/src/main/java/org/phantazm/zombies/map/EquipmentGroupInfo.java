package org.phantazm.zombies.map;

import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.NotNull;

public record EquipmentGroupInfo(@NotNull String defaultItem, @NotNull IntSet slots) {
}
