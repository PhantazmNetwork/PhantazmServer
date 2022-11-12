package com.github.phantazmnetwork.zombies.map.objects;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.commons.vector.Region3I;
import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.zombies.map.*;
import com.github.phantazmnetwork.zombies.map.shop.Shop;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;

public interface MapObjects extends Tickable {

    @Unmodifiable @NotNull List<Spawnpoint> spawnpoints();

    @Unmodifiable @NotNull List<Window> windows();

    @Unmodifiable @NotNull List<Shop> shops();

    @Unmodifiable @NotNull List<Door> doors();

    @Unmodifiable @NotNull List<Room> rooms();

    @Unmodifiable @NotNull List<Round> rounds();

    default @NotNull Optional<Window> nearestWindowInRange(@NotNull Vec3D origin, double distance) {
        double distanceSquared = distance * distance;

        double nearestDistance = Double.POSITIVE_INFINITY;
        Window nearestWindow = null;
        for (Window window : windows()) {
            double currentDistance = window.getCenter().squaredDistance(origin);
            if (currentDistance < nearestDistance) {
                nearestDistance = currentDistance;
                nearestWindow = window;
            }
        }

        if (nearestDistance < distanceSquared) {
            return Optional.of(nearestWindow);
        }

        return Optional.empty();
    }

    default @NotNull Optional<Window> windowAt(@NotNull Vec3I block) {
        for (Window window : windows()) {
            if (window.getData().frameRegion().contains(block)) {
                return Optional.of(window);
            }
        }

        return Optional.empty();
    }

    default @NotNull Optional<Door> doorAt(@NotNull Vec3I block) {
        for (Door door : doors()) {
            Region3I enclosing = door.getEnclosing();
            if (enclosing.contains(block)) {
                for (Region3I subRegion : door.regions()) {
                    if (subRegion.contains(block)) {
                        return Optional.of(door);
                    }
                }
            }
        }

        return Optional.empty();
    }

    default @NotNull Optional<Room> roomAt(@NotNull Vec3I block) {
        for (Room room : rooms()) {
            for (Region3I region : room.roomBounds()) {
                if (region.contains(block)) {
                    return Optional.of(room);
                }
            }
        }

        return Optional.empty();
    }

    default @NotNull Optional<Shop> shopAt(@NotNull Vec3I block) {
        for (Shop shop : shops()) {
            if (block.equals(shop.data.triggerLocation())) {
                return Optional.of(shop);
            }
        }

        return Optional.empty();
    }
}
