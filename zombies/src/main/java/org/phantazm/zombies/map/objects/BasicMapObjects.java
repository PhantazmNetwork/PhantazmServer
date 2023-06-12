package org.phantazm.zombies.map.objects;

import com.github.steanky.element.core.dependency.DependencyProvider;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.tracker.BoundedTracker;
import org.phantazm.mob.spawner.MobSpawner;
import org.phantazm.zombies.map.*;
import org.phantazm.zombies.map.shop.Shop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class BasicMapObjects implements MapObjects {
    private final List<Spawnpoint> spawnpoints;
    private final List<Round> rounds;
    private final DependencyProvider mapDependencyProvider;
    private final Module module;

    private final BoundedTracker<Room> roomTracker;
    private final BoundedTracker<Window> windowTracker;
    private final BoundedTracker<Shop> shopTracker;
    private final BoundedTracker<Door> doorTracker;

    private final MobSpawner mobSpawner;

    private final Map<? super Key, ? extends Room> roomMap;
    private final Point mapOrigin;

    public BasicMapObjects(@NotNull List<Spawnpoint> spawnpoints, @NotNull BoundedTracker<Window> windows,
            @NotNull BoundedTracker<Shop> shops, @NotNull BoundedTracker<Door> doors,
            @NotNull BoundedTracker<Room> rooms, @NotNull List<Round> rounds,
            @NotNull DependencyProvider mapDependencyProvider, @NotNull MobSpawner mobSpawner, @NotNull Point mapOrigin,
            @NotNull Module module) {
        this.spawnpoints = List.copyOf(spawnpoints);
        this.rounds = List.copyOf(rounds);

        this.mapDependencyProvider = Objects.requireNonNull(mapDependencyProvider, "mapDependencyProvider");
        this.module = Objects.requireNonNull(module, "module");

        this.windowTracker = Objects.requireNonNull(windows, "windows");
        this.shopTracker = Objects.requireNonNull(shops, "shops");
        this.doorTracker = Objects.requireNonNull(doors, "doors");
        this.roomTracker = Objects.requireNonNull(rooms, "rooms");

        this.mobSpawner = Objects.requireNonNull(mobSpawner, "mobSpawner");

        Map<Key, Room> map = new HashMap<>(rooms.items().size());
        for (Room room : rooms.items()) {
            map.put(room.getRoomInfo().id(), room);
        }

        this.roomMap = Map.copyOf(map);
        this.mapOrigin = mapOrigin;
    }

    @Override
    public @Unmodifiable @NotNull List<Spawnpoint> spawnpoints() {
        return spawnpoints;
    }

    @Override
    public @Unmodifiable @NotNull List<Round> rounds() {
        return rounds;
    }

    @Override
    public @NotNull DependencyProvider mapDependencyProvider() {
        return mapDependencyProvider;
    }

    @Override
    public @NotNull Module module() {
        return module;
    }

    @Override
    public @NotNull BoundedTracker<Room> roomTracker() {
        return roomTracker;
    }

    @Override
    public @NotNull BoundedTracker<Window> windowTracker() {
        return windowTracker;
    }

    @Override
    public @NotNull BoundedTracker<Shop> shopTracker() {
        return shopTracker;
    }

    @Override
    public @NotNull BoundedTracker<Door> doorTracker() {
        return doorTracker;
    }

    @Override
    public @NotNull Map<? super Key, ? extends Room> roomMap() {
        return roomMap;
    }

    @Override
    public @NotNull MobSpawner mobSpawner() {
        return mobSpawner;
    }

    @Override
    public @NotNull Point mapOrigin() {
        return mapOrigin;
    }
}
