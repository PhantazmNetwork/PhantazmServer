package com.github.phantazmnetwork.zombies.powerup;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.zombies.map.PowerupInfo;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import com.github.steanky.element.core.dependency.DependencyProvider;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface PowerupHandler extends Tickable {
    @NotNull Powerup spawn(@NotNull Key powerupType, double x, double y, double z);

    default @NotNull Powerup spawn(@NotNull Key powerupType, @NotNull Point point) {
        return spawn(powerupType, point.x(), point.y(), point.z());
    }

    @NotNull @UnmodifiableView Collection<Powerup> spawnedOrActivePowerups();

    interface Source {
        @NotNull PowerupHandler make(@NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap,
                @NotNull DependencyProvider mapDependencyProvider);
    }
}
