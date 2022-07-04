package com.github.phantazmnetwork.zombies.equipment.gun.target.tester;

import com.github.phantazmnetwork.api.RayUtils;
import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.PhantazmMob;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class RayTraceTargetTester implements TargetTester {

    public record Data() implements VariantSerializable {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM,"gun.target_tester.ray_trace");

        @Override
        public @NotNull Key getSerialKey() {
            return SERIAL_KEY;
        }

    }

    private final Data data;

    public RayTraceTargetTester(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull Optional<Vec> getHitLocation(@NotNull Player player, @NotNull PhantazmMob mob,
                                                 @NotNull Pos start) {
        Entity entity = mob.entity();
        return RayUtils.rayTrace(entity.getBoundingBox(), entity.getPosition(), start);
    }

    @Override
    public @NotNull VariantSerializable getData() {
        return data;
    }
}
