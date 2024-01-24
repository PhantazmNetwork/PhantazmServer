package org.phantazm.mob2;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record MobMeta(@Nullable Component customName,
    boolean customNameVisible,
    boolean isInvisible,
    boolean isGlowing,
    int angerTime,
    boolean isBaby,
    int size,
    ItemStack itemStack) {
    @Default("customName")
    public static @NotNull ConfigElement defaultCustomName() {
        return ConfigPrimitive.NULL;
    }

    @Default("customNameVisible")
    public static @NotNull ConfigElement defaultCustomNameVisible() {
        return ConfigPrimitive.of(false);
    }

    @Default("isInvisible")
    public static @NotNull ConfigElement defaultIsInvisible() {
        return ConfigPrimitive.of(false);
    }

    @Default("isGlowing")
    public static @NotNull ConfigElement defaultIsGlowing() {
        return ConfigPrimitive.of(false);
    }

    @Default("angerTime")
    public static @NotNull ConfigElement defaultAngerTime() {
        return ConfigPrimitive.of(-1);
    }

    @Default("isBaby")
    public static @NotNull ConfigElement defaultIsBaby() {
        return ConfigPrimitive.of(false);
    }

    @Default("size")
    public static @NotNull ConfigElement defaultSize() {
        return ConfigPrimitive.of(0);
    }

    @Default("itemStack")
    public static @NotNull ConfigElement defaultItemStack() {
        return ConfigPrimitive.NULL;
    }
}
