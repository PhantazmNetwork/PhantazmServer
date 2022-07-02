package com.github.phantazmnetwork.zombies.equipment.gun.shot.handler;

import com.github.phantazmnetwork.zombies.equipment.gun.Gun;
import com.github.phantazmnetwork.zombies.equipment.gun.effect.GunEffect;
import com.github.phantazmnetwork.zombies.equipment.gun.shot.GunShot;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GunEffectShotHandler implements ShotHandler {

    private final GunEffect effect;

    public GunEffectShotHandler(@NotNull GunEffect effect) {
        this.effect = effect;
    }

    @Override
    public void tick(long time) {
        effect.tick(time);
    }

    @Override
    public void handle(@NotNull Gun gun, @NotNull Player attacker, @NotNull GunShot shot) {
        effect.accept(gun);
    }

    @Override
    public @NotNull Key getSerialKey() {
        return effect.getSerialKey();
    }
}
