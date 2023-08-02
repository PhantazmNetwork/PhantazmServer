package org.phantazm.zombies.powerup2;

import com.github.steanky.element.core.dependency.DependencyProvider;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.commons.Tickable;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene.ZombiesScene;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Supplier;

public interface PowerupHandler extends Tickable {
    @NotNull Powerup spawn(@NotNull Key powerupType, double x, double y, double z);

    boolean typeExists(@NotNull Key powerupType);

    default @NotNull Powerup spawn(@NotNull Key powerupType, @NotNull Point point) {
        return spawn(powerupType, point.x(), point.y(), point.z());
    }

    default @NotNull Optional<Powerup> spawnIfExists(@NotNull Key powerupType, double x, double y, double z,
            @NotNull Logger logger) {
        Objects.requireNonNull(logger, "logger");
        if (typeExists(powerupType)) {
            return Optional.of(spawn(powerupType, x, y, z));
        }
        else {
            logger.warn("Powerup of type " + powerupType + " does not exist");
            return Optional.empty();
        }
    }

    default @NotNull Optional<Powerup> spawnIfExists(@NotNull Key powerupType, @NotNull Point point,
            @NotNull Logger logger) {
        return spawnIfExists(powerupType, point.x(), point.y(), point.z(), logger);
    }

    @NotNull @UnmodifiableView Collection<Powerup> spawnedOrActivePowerups();

    interface Source {
        @NotNull PowerupHandler make(@NotNull ZombiesScene scene);
    }
}
