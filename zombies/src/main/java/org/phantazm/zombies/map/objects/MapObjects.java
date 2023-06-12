package org.phantazm.zombies.map.objects;

import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.gui.SlotDistributor;
import org.phantazm.core.sound.SongPlayer;
import org.phantazm.core.tracker.BoundedTracker;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.spawner.MobSpawner;
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

    @Unmodifiable @NotNull List<Round> rounds();

    @NotNull DependencyProvider mapDependencyProvider();

    @NotNull Module module();

    @NotNull BoundedTracker<Room> roomTracker();

    @NotNull BoundedTracker<Window> windowTracker();

    @NotNull BoundedTracker<Shop> shopTracker();

    @NotNull BoundedTracker<Door> doorTracker();

    @NotNull Map<? super Key, ? extends Room> roomMap();

    @NotNull MobSpawner mobSpawner();

    @NotNull Point mapOrigin();

    interface Source {
        @NotNull MapObjects make(@NotNull Instance instance,
                @NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap,
                @NotNull Supplier<? extends RoundHandler> roundHandlerSupplier, @NotNull MobStore mobStore,
                @NotNull Wrapper<PowerupHandler> powerupHandler, @NotNull Wrapper<WindowHandler> windowHandler,
                @NotNull Wrapper<EventNode<Event>> eventNode, @NotNull SongPlayer songPlayer);
    }

    interface Module {
        @NotNull KeyParser keyParser();

        @NotNull Instance instance();

        @NotNull Random random();

        @NotNull Supplier<? extends RoundHandler> roundHandlerSupplier();

        @NotNull Flaggable flags();

        @NotNull TransactionModifierSource modifierSource();

        @NotNull SlotDistributor slotDistributor();

        @NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap();

        @NotNull Collection<? extends ZombiesPlayer> playerCollection();

        @NotNull Pos respawnPos();

        @NotNull Supplier<? extends MapObjects> mapObjectsSupplier();

        @NotNull Supplier<? extends PowerupHandler> powerupHandler();

        @NotNull Supplier<? extends WindowHandler> windowHandler();

        @NotNull Supplier<? extends EventNode<Event>> eventNode();

        @NotNull MobStore mobStore();

        @NotNull SongPlayer songPlayer();
    }
}
