package com.github.phantazmnetwork.zombies.equipment.gun.shot.handler;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.equipment.gun.Gun;
import com.github.phantazmnetwork.zombies.equipment.gun.shot.GunShot;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class IgniteShotHandler implements ShotHandler {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.hit_handler.ignite");

    private final int duration;

    public IgniteShotHandler(int duration) {
        this.duration = duration;
    }

    @Override
    public void handle(@NotNull Gun gun, @NotNull Player attacker, @NotNull GunShot shot) {
        for (Pair<PhantazmMob, Vec> target : shot.getRegularTargets()) {
            target.left().entity().setFireForDuration(duration);
        }
        for (Pair<PhantazmMob, Vec> target : shot.getHeadshotTargets()) {
            target.left().entity().setFireForDuration(duration);
        }
    }

    @Override
    public void tick(long time) {

    }

    @Override
    public @NotNull Key getSerialKey() {
        return SERIAL_KEY;
    }

}
