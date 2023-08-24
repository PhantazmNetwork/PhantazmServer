package org.phantazm.mob2;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record MobData(@NotNull Key key,
    @NotNull EntityType type,
    @NotNull ConfigNode pathfinding,
    @NotNull ConfigNode equipment,
    @NotNull ConfigNode attributes,
    @Nullable MobMeta meta,
    @NotNull List<ConfigNode> skills,
    @NotNull List<ConfigNode> goals,
    @NotNull ConfigNode extra) implements Keyed {
    @Default("equipment")
    public static @NotNull ConfigElement defaultEquipment() {
        return ConfigList.of();
    }

    @Default("attributes")
    public static @NotNull ConfigElement defaultAttributes() {
        return ConfigList.of();
    }

    @Default("meta")
    public static @NotNull ConfigElement defaultMeta() {
        return ConfigPrimitive.NULL;
    }

    @Default("skills")
    public static @NotNull ConfigElement defaultSkills() {
        return ConfigList.of();
    }

    @Default("goals")
    public static @NotNull ConfigElement defaultGoals() {
        return ConfigList.of();
    }

    @Default("extra")
    public static @NotNull ConfigElement defaultExtra() {
        return ConfigNode.of();
    }
}
