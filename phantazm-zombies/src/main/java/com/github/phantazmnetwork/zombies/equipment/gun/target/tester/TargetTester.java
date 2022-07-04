package com.github.phantazmnetwork.zombies.equipment.gun.target.tester;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.mob.PhantazmMob;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface TargetTester {

    @NotNull Optional<Vec> getHitLocation(@NotNull Player player, @NotNull PhantazmMob mob, @NotNull Pos start);

    @NotNull VariantSerializable getData();

}
