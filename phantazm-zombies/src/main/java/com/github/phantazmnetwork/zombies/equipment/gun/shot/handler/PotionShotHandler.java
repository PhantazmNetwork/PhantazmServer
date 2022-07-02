package com.github.phantazmnetwork.zombies.equipment.gun.shot.handler;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.equipment.gun.Gun;
import com.github.phantazmnetwork.zombies.equipment.gun.shot.GunShot;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.potion.Potion;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PotionShotHandler implements ShotHandler {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.hit_handler.potion");

    private final Potion potion;

    public PotionShotHandler(@NotNull Potion potion) {
        this.potion = Objects.requireNonNull(potion, "potion");
    }

    @Override
    public void handle(@NotNull Gun gun, @NotNull Player attacker, @NotNull GunShot shot) {
        for (Pair<PhantazmMob, Vec> target : shot.getRegularTargets()) {
            target.left().entity().addEffect(potion);
        }
        for (Pair<PhantazmMob, Vec> target : shot.getHeadshotTargets()) {
            target.left().entity().addEffect(potion);
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
