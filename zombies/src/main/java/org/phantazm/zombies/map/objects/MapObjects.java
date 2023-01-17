package org.phantazm.zombies.map.objects;

import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.toolkit.collection.Wrapper;
import com.github.steanky.vector.Bounds3I;
import com.github.steanky.vector.Vec3I;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.VecUtils;
import org.phantazm.core.gui.SlotDistributor;
import org.phantazm.mob.MobStore;
import org.phantazm.zombies.coin.TransactionModifierSource;
import org.phantazm.zombies.map.*;
import org.phantazm.zombies.map.handler.RoundHandler;
import org.phantazm.zombies.map.handler.WindowHandler;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.PowerupHandler;

import java.util.*;
import java.util.function.Supplier;

public interface MapObjects {
    @Unmodifiable @NotNull List<Spawnpoint> spawnpoints();

    @Unmodifiable @NotNull List<Window> windows();

    @Unmodifiable @NotNull List<Shop> shops();

    @Unmodifiable @NotNull List<Door> doors();

    @Unmodifiable @NotNull List<Room> rooms();

    @Unmodifiable @NotNull List<Round> rounds();

    @NotNull DependencyProvider mapDependencyProvider();

    @NotNull Module module();

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
            Bounds3I enclosing = door.getEnclosing();
            if (enclosing.contains(vec)) {
                for (Bounds3I subRegion : door.regions()) {
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
            for (Bounds3I region : room.roomBounds()) {
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
            if (block.sameBlock(trigger.x(), trigger.y(), trigger.z())) {
                return Optional.of(shop);
            }
        }

        return Optional.empty();
    }

    interface Source {
        @NotNull MapObjects make(@NotNull Instance instance,
                @NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap,
                @NotNull Supplier<? extends RoundHandler> roundHandlerSupplier, @NotNull MobStore mobStore,
                @NotNull Wrapper<PowerupHandler> powerupHandler, @NotNull Wrapper<WindowHandler> windowHandler);
    }

    interface Module {
        @NotNull Instance instance();

        @NotNull Random random();

        @NotNull Supplier<? extends RoundHandler> roundHandlerSupplier();

        @NotNull Flaggable flaggable();

        @NotNull TransactionModifierSource modifierSource();

        @NotNull SlotDistributor slotDistributor();

        @NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap();

        @NotNull Collection<? extends ZombiesPlayer> playerCollection();

        @NotNull Pos respawnPos();

        @NotNull Supplier<? extends MapObjects> mapObjectsSupplier();

        @NotNull Supplier<? extends PowerupHandler> powerupHandler();

        @NotNull Supplier<? extends WindowHandler> windowHandler();

        @NotNull MobStore mobStore();
    }
}
