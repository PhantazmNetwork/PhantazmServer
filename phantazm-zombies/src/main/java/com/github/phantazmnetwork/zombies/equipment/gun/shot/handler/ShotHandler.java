package com.github.phantazmnetwork.zombies.equipment.gun.shot.handler;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.zombies.equipment.gun.shot.GunShot;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface ShotHandler extends VariantSerializable {

    void handle(@NotNull Player attacker, @NotNull GunShot shot);

}
