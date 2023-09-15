package org.phantazm.zombies.powerup;

import net.kyori.adventure.key.Key;
import net.minestom.server.Tickable;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

public interface PowerupHandler extends Tickable {
    @NotNull
    Powerup spawn(@NotNull Key powerupType, double x, double y, double z);

    void assignPowerup(@NotNull LivingEntity livingEntity, @NotNull Key powerupKey);

    boolean canSpawnType(@NotNull Key powerupType);

    void end();

    default @NotNull Powerup spawn(@NotNull Key powerupType, @NotNull Point point) {
        return spawn(powerupType, point.x(), point.y(), point.z());
    }

    default @NotNull Optional<Powerup> spawnIfExists(@NotNull Key powerupType, double x, double y, double z) {
        if (canSpawnType(powerupType)) {
            return Optional.of(spawn(powerupType, x, y, z));
        }

        return Optional.empty();
    }

    @SuppressWarnings("UnusedReturnValue")
    default @NotNull Optional<Powerup> spawnIfExists(@NotNull Key powerupType, @NotNull Point point) {
        return spawnIfExists(powerupType, point.x(), point.y(), point.z());
    }

    @NotNull
    @UnmodifiableView
    Collection<Powerup> spawnedOrActivePowerups();

    interface Source {
        @NotNull
        PowerupHandler make(@NotNull Supplier<? extends @NotNull ZombiesScene> scene);
    }
}
