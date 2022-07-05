package com.github.phantazmnetwork.zombies.equipment.gun.target.tester;

import com.github.phantazmnetwork.api.RayUtils;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class RayTraceTargetTester implements TargetTester {

    public record Data() implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM,"gun.target_tester.ray_trace");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }

    }

    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) {
                return new Data();
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) {
                return new LinkedConfigNode(0);
            }
        };
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
    public @NotNull Keyed getData() {
        return data;
    }
}
