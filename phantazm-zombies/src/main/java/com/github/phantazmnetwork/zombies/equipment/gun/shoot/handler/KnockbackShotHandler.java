package com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunShot;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public class KnockbackShotHandler implements ShotHandler {

    public record Data(double knockback, double headshotKnockback) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.shot_handler.knockback");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                double knockback = element.getNumberOrThrow("knockback").doubleValue();
                double headshotKnockback = element.getNumberOrThrow("headshotKnockback").doubleValue();
                return new Data(knockback, headshotKnockback);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(2);
                node.putNumber("knockback", data.knockback());
                node.putNumber("headshotKnockback", data.headshotKnockback());
                return node;
            }
        };
    }

    private final Data data;

    public KnockbackShotHandler(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public void handle(@NotNull GunState state, @NotNull Entity attacker, @NotNull Collection<UUID> previousHits, @NotNull GunShot shot) {
        Pos start = attacker.getPosition().add(0, attacker.getEyeHeight(), 0);
        for (GunHit target : shot.regularTargets()) {
            Entity entity = target.entity();
            Vec knockbackVec = target.location().sub(start).normalize().mul(data.knockback());
            entity.setVelocity(entity.getVelocity().add(knockbackVec));
        }
        for (GunHit target : shot.headshotTargets()) {
            Entity entity = target.entity();
            Vec knockbackVec = target.location().sub(start).normalize().mul(data.headshotKnockback());
            entity.setVelocity(entity.getVelocity().add(knockbackVec));
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

}
