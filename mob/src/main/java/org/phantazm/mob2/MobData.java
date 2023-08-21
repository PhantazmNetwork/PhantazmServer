package org.phantazm.mob2;

import com.github.steanky.ethylene.core.collection.ConfigNode;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;

public record MobData(@NotNull Key key,
    @NotNull EntityType type,
    @NotNull Map<EquipmentSlot, ItemStack> equipment,
    @NotNull Object2FloatMap<String> attributes,
    @Nullable MobMeta meta,
    @NotNull List<ConfigNode> skills,
    @NotNull List<ConfigNode> goals,
    @NotNull @Unmodifiable ConfigNode extra) implements Keyed {
}
