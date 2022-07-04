package com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.equipment.gun.effect.GunEffect;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunShot;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class GunEffectShotHandler implements ShotHandler {

    private final GunEffect effect;

    public GunEffectShotHandler(@NotNull GunEffect effect) {
        this.effect = effect;
    }

    @Override
    public void tick(@NotNull GunState state, long time) {
        effect.tick(state, time);
    }

    @Override
    public @NotNull VariantSerializable getData() {
        return effect.getData();
    }

    @Override
    public void handle(@NotNull GunState state, @NotNull Player attacker, @NotNull Collection<PhantazmMob> previousHits, @NotNull GunShot shot) {
        effect.accept(state);
    }

}
