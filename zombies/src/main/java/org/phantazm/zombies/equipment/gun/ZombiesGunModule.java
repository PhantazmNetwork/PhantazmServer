package org.phantazm.zombies.equipment.gun;

import com.github.steanky.element.core.annotation.Depend;
import com.github.steanky.element.core.annotation.Memoize;
import com.github.steanky.element.core.dependency.DependencyModule;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.spawner.MobSpawner;
import org.phantazm.zombies.map.objects.MapObjects;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

@SuppressWarnings("ClassCanBeRecord")
@Memoize
@Depend
public class ZombiesGunModule implements DependencyModule {

    private final PlayerView playerView;

    private final MobSpawner mobSpawner;

    private final MobStore mobStore;

    private final EventNode<Event> eventNode;

    private final Random random;

    private final MapObjects mapObjects;

    public ZombiesGunModule(@NotNull PlayerView playerView, @NotNull MobSpawner mobSpawner, @NotNull MobStore mobStore,
            @NotNull EventNode<Event> eventNode, @NotNull Random random, @NotNull MapObjects mapObjects) {
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.mobSpawner = Objects.requireNonNull(mobSpawner, "mobSpawner");
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
        this.eventNode = Objects.requireNonNull(eventNode, "eventNode");
        this.random = Objects.requireNonNull(random, "random");
        this.mapObjects = Objects.requireNonNull(mapObjects, "mapObjects");
    }

    public @NotNull PlayerView getPlayerView() {
        return playerView;
    }

    public @NotNull Supplier<Optional<? extends Entity>> getShooterSupplier() {
        return playerView::getPlayer;
    }

    public @NotNull UUID getShooterUUID() {
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
}
