package com.github.phantazmnetwork.zombies.map.objects;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.commons.vector.Region3I;
import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.core.VecUtils;
import com.github.phantazmnetwork.zombies.map.*;
import com.github.phantazmnetwork.zombies.map.shop.Shop;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import com.github.steanky.element.core.dependency.DependencyModule;
import com.github.steanky.element.core.dependency.DependencyProvider;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public interface MapObjects extends Tickable {
    interface Source {
        @NotNull MapObjects make(@NotNull Instance instance, @NotNull MapInfo mapInfo,
                @NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap,
                @NotNull Supplier<? extends RoundHandler> roundHandlerSupplier);
    }

    @Unmodifiable @NotNull List<Spawnpoint> spawnpoints();

    @Unmodifiable @NotNull List<Window> windows();

    @Unmodifiable @NotNull List<Shop> shops();

    @Unmodifiable @NotNull List<Door> doors();

    @Unmodifiable @NotNull List<Room> rooms();

    @Unmodifiable @NotNull List<Round> rounds();

    @NotNull DependencyModule module();

    @NotNull DependencyProvider mapDependencyProvider();

    default @NotNull Optional<Window> nearestWindowInRange(@NotNull Point origin, double distance) {
        double distanceSquared = distance * distance;

        double nearestDistance = Double.POSITIVE_INFINITY;
        Window nearestWindow = null;
        for (Window window : windows()) {
            double currentDistance = window.getCenter().distanceSquared(origin);
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

    default @NotNull Optional<Window> windowAt(@NotNull Point block) {
        Vec3I vec = VecUtils.toBlockInt(block);
        for (Window window : windows()) {
            if (window.getWindowInfo().frameRegion().contains(vec)) {
                return Optional.of(window);
            }
        }

        return Optional.empty();
    }

    default @NotNull Optional<Door> doorAt(@NotNull Point block) {
        Vec3I vec = VecUtils.toBlockInt(block);
        for (Door door : doors()) {
            Region3I enclosing = door.getEnclosing();
            if (enclosing.contains(vec)) {
                for (Region3I subRegion : door.regions()) {
                    if (subRegion.contains(vec)) {
                        return Optional.of(door);
                    }
                }
            }
        }

        return Optional.empty();
    }

    default @NotNull Optional<Room> roomAt(@NotNull Point block) {
        Vec3I vec = VecUtils.toBlockInt(block);
        for (Room room : rooms()) {
            for (Region3I region : room.roomBounds()) {
                if (region.contains(vec)) {
                    return Optional.of(room);
                }
            }
        }

        return Optional.empty();
    }

    default @NotNull Optional<Shop> shopAt(@NotNull Point block) {
        for (Shop shop : shops()) {
            Vec3I trigger = shop.getShopInfo().triggerLocation();
            if (block.sameBlock(trigger.getX(), trigger.getY(), trigger.getZ())) {
                return Optional.of(shop);
            }
        }

        return Optional.empty();
    }
}
