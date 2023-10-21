package org.phantazm.zombies.map;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.NotNull;

public record EquipmentGroupInfo(@NotNull String defaultItem,
    @NotNull IntSet slots) {
    @Default("defaultItem")
    public static @NotNull ConfigElement defaultDefaultItem() {
        return ConfigPrimitive.of("");
    }
}
