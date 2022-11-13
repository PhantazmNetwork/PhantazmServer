package com.github.phantazmnetwork.zombies.powerup;

import com.github.phantazmnetwork.commons.Tickable;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;

public interface PowerupHandler extends Tickable {
    @NotNull Powerup spawn(@NotNull Key powerupType, double x, double y, double z);

    @NotNull @UnmodifiableView Collection<Powerup> spawnedOrActivePowerups();
}
