package com.github.phantazmnetwork.zombies.equipment.gun.shot.handler;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.equipment.gun.shot.GunShot;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import org.jetbrains.annotations.NotNull;

public class DamageShotHandler implements ShotHandler {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.hit_handler.damage");

    private final float damage;

    private final float headshotDamage;

    public DamageShotHandler(float damage, float headshotDamage) {
        this.damage = damage;
        this.headshotDamage = headshotDamage;
    }

    @Override
    public void handle(@NotNull Player attacker, @NotNull GunShot shot) {
        for (Pair<PhantazmMob, Vec> target : shot.getRegularTargets()) {
            target.left().entity().damage(DamageType.fromPlayer(attacker), damage);
        }
        for (Pair<PhantazmMob, Vec> target : shot.getHeadshotTargets()) {
            target.left().entity().damage(DamageType.fromPlayer(attacker), headshotDamage);
        }
    }

    @Override
    public @NotNull Key getSerialKey() {
        return SERIAL_KEY;
    }
}
