package com.github.phantazmnetwork.zombies.equipment.gun;

import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.mob.spawner.MobSpawner;
import com.github.phantazmnetwork.zombies.game.map.objects.MapObjects;
import com.github.steanky.element.core.annotation.DependencySupplier;
import com.github.steanky.element.core.annotation.Memoize;
import com.github.steanky.element.core.dependency.DependencyModule;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

@SuppressWarnings("ClassCanBeRecord")
public class ZombiesGunModule implements DependencyModule {

    private final PlayerView playerView;

    private final MobSpawner mobSpawner;

    private final MobStore mobStore;

    private final Random random;

    private final Supplier<? extends MapObjects> mapObjects;

    public ZombiesGunModule(@NotNull PlayerView playerView, @NotNull MobSpawner mobSpawner, @NotNull MobStore mobStore,
            @NotNull Random random, @NotNull Supplier<? extends MapObjects> mapObjects) {
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.mobSpawner = Objects.requireNonNull(mobSpawner, "mobSpawner");
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
        this.random = Objects.requireNonNull(random, "random");
        this.mapObjects = Objects.requireNonNull(mapObjects, "mapObjects");
    }

    @Memoize
    @DependencySupplier("zombies.dependency.gun.player_view")
    public @NotNull PlayerView getPlayerView() {
        return playerView;
    }

    // TODO: maybe make this name-key based for EntityInstanceAudienceProvider
    @Memoize
    @DependencySupplier("zombies.dependency.gun.entity_supplier")
    public @NotNull Supplier<Optional<? extends Entity>> getEntitySupplier() {
        return getShooterSupplier();
    }

    @Memoize
    @DependencySupplier("zombies.dependency.gun.shooter.supplier")
    public @NotNull Supplier<Optional<? extends Entity>> getShooterSupplier() {
        return playerView::getPlayer;
    }

    @Memoize
    @DependencySupplier("zombies.dependency.gun.shooter.uuid")
    public @NotNull UUID getShooterUUID() {
        return playerView.getUUID();
    }

    // TODO: move everything under to different modules?
    // TODO
    @Memoize
    @DependencySupplier("zombies.dependency.mob.spawner")
    public @NotNull MobSpawner getMobSpawner() {
        return mobSpawner;
    }

    @Memoize
    @DependencySupplier("zombies.dependency.mob.store")
    public @NotNull MobStore getMobStore() {
        return mobStore;
    }

    @Memoize
    @DependencySupplier("zombies.dependency.random")
    public @NotNull Random getRandom() {
        return random;
    }

    @Memoize
    @DependencySupplier("zombies.dependency.map_object.map_objects")
    public @NotNull Supplier<? extends MapObjects> getMapObjects() {
        return mapObjects;
    }
}
