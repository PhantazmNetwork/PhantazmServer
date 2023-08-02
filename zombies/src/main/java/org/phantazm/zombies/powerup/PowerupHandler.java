package org.phantazm.zombies.powerup;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.commons.Tickable;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.*;
import java.util.function.Supplier;

public interface PowerupHandler extends Tickable {
    @NotNull Powerup spawn(@NotNull Key powerupType, double x, double y, double z);

    boolean canSpawnType(@NotNull Key powerupType);

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

    @NotNull @UnmodifiableView Collection<Powerup> spawnedOrActivePowerups();

    interface Source {
        @NotNull PowerupHandler make(@NotNull Supplier<? extends @NotNull ZombiesScene> scene);
    }
}
