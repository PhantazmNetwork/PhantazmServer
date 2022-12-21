package org.phantazm.zombies.map.objects;

import com.github.steanky.element.core.dependency.DependencyProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.zombies.map.*;
import org.phantazm.zombies.map.shop.Shop;

import java.util.List;
import java.util.Objects;

public record BasicMapObjects(@Unmodifiable @NotNull List<Spawnpoint> spawnpoints,
                              @Unmodifiable @NotNull List<Window> windows,
                              @Unmodifiable @NotNull List<Shop> shops,
                              @Unmodifiable @NotNull List<Door> doors,
                              @Unmodifiable @NotNull List<Room> rooms,
                              @Unmodifiable @NotNull List<Round> rounds,
                              @NotNull DependencyProvider mapDependencyProvider,
                              @NotNull Module module) implements MapObjects {
    public BasicMapObjects(@NotNull List<Spawnpoint> spawnpoints, @NotNull List<Window> windows,
            @NotNull List<Shop> shops, @NotNull List<Door> doors, @NotNull List<Room> rooms,
            @NotNull List<Round> rounds, @NotNull DependencyProvider mapDependencyProvider, @NotNull Module module) {
        this.spawnpoints = List.copyOf(spawnpoints);
        this.windows = List.copyOf(windows);
        this.shops = List.copyOf(shops);
        this.doors = List.copyOf(doors);
        this.rooms = List.copyOf(rooms);
        this.rounds = List.copyOf(rounds);
        this.mapDependencyProvider = Objects.requireNonNull(mapDependencyProvider, "mapDependencyProvider");
        this.module = Objects.requireNonNull(module, "module");
    }
}
