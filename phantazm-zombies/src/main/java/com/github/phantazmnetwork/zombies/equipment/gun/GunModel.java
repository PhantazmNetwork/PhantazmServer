package com.github.phantazmnetwork.zombies.equipment.gun;

import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public record GunModel(@NotNull List<GunLevel> levels, @NotNull Key name) implements Keyed {

    public GunModel {
        Objects.requireNonNull(levels, "levels");
        Objects.requireNonNull(name, "serialKey");
    }

    @Override
    public @NotNull Key key() {
        return name;
    }
}
