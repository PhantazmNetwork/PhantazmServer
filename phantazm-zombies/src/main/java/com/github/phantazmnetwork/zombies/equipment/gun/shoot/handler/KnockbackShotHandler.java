package com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunShot;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class KnockbackShotHandler implements ShotHandler {


    public record Data(double knockback, double headshotKnockback) implements VariantSerializable {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.hit_handler.knockback");

        @Override
        public @NotNull Key getSerialKey() {
            return SERIAL_KEY;
        }
    }

    private final Data data;

    public KnockbackShotHandler(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public void handle(@NotNull GunState state, @NotNull Player attacker, @NotNull Collection<PhantazmMob> previousHits, @NotNull GunShot shot) {
        Pos start = attacker.getPosition().add(0, attacker.getEyeHeight(), 0);
        for (GunHit target : shot.regularTargets()) {
            Entity entity = target.mob().entity();
            Vec knockbackVec = target.location().sub(start).normalize().mul(data.knockback());
            entity.setVelocity(entity.getVelocity().add(knockbackVec));
        }
        for (GunHit target : shot.headshotTargets()) {
            Entity entity = target.mob().entity();
            Vec knockbackVec = target.location().sub(start).normalize().mul(data.headshotKnockback());
            entity.setVelocity(entity.getVelocity().add(knockbackVec));
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    @Override
    public @NotNull VariantSerializable getData() {
        return data;
    }

}
