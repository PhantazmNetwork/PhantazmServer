package com.github.phantazmnetwork.zombies.equipment.gun;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public record GunModel(@NotNull List<GunLevel> levels, @NotNull Key serialKey) implements VariantSerializable {

    public GunModel {
        Objects.requireNonNull(levels, "levels");
        Objects.requireNonNull(serialKey, "serialKey");
    }

    @Override
    public @NotNull Key getSerialKey() {
        return serialKey;
    }
}
