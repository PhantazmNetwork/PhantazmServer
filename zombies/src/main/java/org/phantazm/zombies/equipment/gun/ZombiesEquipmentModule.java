package org.phantazm.zombies.equipment.gun;

import com.github.steanky.element.core.annotation.Depend;
import com.github.steanky.element.core.annotation.Memoize;
import com.github.steanky.element.core.dependency.DependencyModule;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.mob.MobModel;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.spawner.MobSpawner;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("ClassCanBeRecord")
@Memoize
@Depend
public class ZombiesEquipmentModule implements DependencyModule {
    private final Map<? super UUID, ? extends ZombiesPlayer> playerMap;
    private final PlayerView playerView;
    private final MobSpawner mobSpawner;
    private final MobStore mobStore;
    private final EventNode<Event> eventNode;
    private final Random random;
    private final MapObjects mapObjects;
    private final Supplier<? extends ZombiesPlayer> zombiesPlayerSupplier;
    private final Function<? super Key, ? extends MobModel> mobModelFunction;

    public ZombiesEquipmentModule(@NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap,
            @NotNull PlayerView playerView, @NotNull MobSpawner mobSpawner, @NotNull MobStore mobStore,
            @NotNull EventNode<Event> eventNode, @NotNull Random random, @NotNull MapObjects mapObjects,
            @NotNull Supplier<? extends ZombiesPlayer> zombiesPlayerSupplier,
            @NotNull Function<? super Key, ? extends MobModel> mobModelFunction) {
        this.playerMap = Objects.requireNonNull(playerMap, "playerMap");
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.mobSpawner = Objects.requireNonNull(mobSpawner, "mobSpawner");
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
        this.eventNode = Objects.requireNonNull(eventNode, "eventNode");
        this.random = Objects.requireNonNull(random, "random");
        this.mapObjects = Objects.requireNonNull(mapObjects, "mapObjects");
        this.zombiesPlayerSupplier = Objects.requireNonNull(zombiesPlayerSupplier, "zombiesPlayerSupplier");
        this.mobModelFunction = Objects.requireNonNull(mobModelFunction, "mobModelFunction");
    }

    public @NotNull Map<? super UUID, ? extends ZombiesPlayer> getPlayerMap() {
        return playerMap;
    }

    public @NotNull PlayerView getPlayerView() {
        return playerView;
    }

    public @NotNull Supplier<Optional<? extends Entity>> getPlayerSupplier() {
        return playerView::getPlayer;
    }

    public @NotNull UUID getPlayerUUID() {
        return playerView.getUUID();
    }

    public @NotNull MobSpawner getMobSpawner() {
        return mobSpawner;
    }

    public @NotNull MobStore getMobStore() {
        return mobStore;
    }

    public @NotNull EventNode<Event> getEventNode() {
        return eventNode;
    }

    public @NotNull Random getRandom() {
        return random;
    }

    public @NotNull MapObjects getMapObjects() {
        return mapObjects;
    }

    public @NotNull Supplier<? extends ZombiesPlayer> getZombiesPlayerSupplier() {
        return zombiesPlayerSupplier;
    }

    public @NotNull Function<? super Key, ? extends MobModel> getMobModelFunction() {
        return mobModelFunction;
    }
}
