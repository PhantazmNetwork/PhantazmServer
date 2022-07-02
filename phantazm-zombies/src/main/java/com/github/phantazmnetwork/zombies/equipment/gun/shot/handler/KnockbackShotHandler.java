package com.github.phantazmnetwork.zombies.equipment.gun.shot.handler;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.equipment.gun.shot.GunShot;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KnockbackShotHandler implements ShotHandler {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.hit_handler.knockback");

    private final double knockback;

    private final double headshotKnockback;

    public KnockbackShotHandler(double knockback, double headshotKnockback) {
        this.knockback = knockback;
        this.headshotKnockback = headshotKnockback;
    }

    @Override
    public void handle(@NotNull Player attacker, @NotNull GunShot shot) {
        Pos start = attacker.getPosition().add(0, attacker.getEyeHeight(), 0);
        for (Pair<PhantazmMob, Vec> target : shot.getRegularTargets()) {
            Entity entity = target.left().entity();
            Vec knockbackVec = target.right().sub(start).normalize().mul(knockback);
            entity.setVelocity(entity.getVelocity().add(knockbackVec));
        }
        for (Pair<PhantazmMob, Vec> target : shot.getHeadshotTargets()) {
            Entity entity = target.left().entity();
            Vec knockbackVec = target.right().sub(start).normalize().mul(headshotKnockback);
            entity.setVelocity(entity.getVelocity().add(knockbackVec));
        }
    }

    @Override
    public @NotNull Key getSerialKey() {
        return SERIAL_KEY;
    }
}
