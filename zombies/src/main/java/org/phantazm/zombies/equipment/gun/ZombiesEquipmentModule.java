package org.phantazm.zombies.equipment.gun;

import com.github.steanky.element.core.annotation.Depend;
import com.github.steanky.element.core.annotation.Memoize;
import com.github.steanky.element.core.dependency.DependencyModule;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.mob2.MobSpawner;
import org.phantazm.stats.zombies.ZombiesPlayerMapStats;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.action_bar.ZombiesPlayerActionBar;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.*;
import java.util.function.Supplier;

@Memoize
@Depend
public class ZombiesEquipmentModule implements DependencyModule {
    private final ZombiesScene zombiesScene;
    private final Map<PlayerView, ZombiesPlayer> playerMap;
    private final PlayerView playerView;
    private final ZombiesPlayerMapStats mapStats;
    private final ZombiesPlayerActionBar actionBar;
    private final MobSpawner mobSpawner;
    private final EventNode<Event> eventNode;
    private final Random random;
    private final MapObjects mapObjects;
    private final Supplier<? extends ZombiesPlayer> zombiesPlayerSupplier;

    public ZombiesEquipmentModule(@NotNull ZombiesScene zombiesScene, @NotNull Map<PlayerView, ZombiesPlayer> playerMap,
        @NotNull PlayerView playerView, @NotNull ZombiesPlayerMapStats mapStats,
        @NotNull ZombiesPlayerActionBar actionBar, @NotNull MobSpawner mobSpawner,
        @NotNull EventNode<Event> eventNode, @NotNull Random random,
        @NotNull MapObjects mapObjects, @NotNull Supplier<? extends ZombiesPlayer> zombiesPlayerSupplier) {
        this.zombiesScene = Objects.requireNonNull(zombiesScene);
        this.playerMap = Objects.requireNonNull(playerMap);
        this.playerView = Objects.requireNonNull(playerView);
        this.mapStats = Objects.requireNonNull(mapStats);
        this.actionBar = Objects.requireNonNull(actionBar);
        this.mobSpawner = Objects.requireNonNull(mobSpawner);
        this.eventNode = Objects.requireNonNull(eventNode);
        this.random = Objects.requireNonNull(random);
        this.mapObjects = Objects.requireNonNull(mapObjects);
        this.zombiesPlayerSupplier = Objects.requireNonNull(zombiesPlayerSupplier);
    }

    public @NotNull ZombiesScene getScene() {
        return zombiesScene;
    }

    public @NotNull Map<PlayerView, ZombiesPlayer> getPlayerMap() {
        return playerMap;
    }

    public @NotNull PlayerView getPlayerView() {
        return playerView;
    }

    public @NotNull ZombiesPlayerMapStats getMapStats() {
        return mapStats;
    }

    public @NotNull ZombiesPlayerActionBar getActionBar() {
        return actionBar;
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
}
