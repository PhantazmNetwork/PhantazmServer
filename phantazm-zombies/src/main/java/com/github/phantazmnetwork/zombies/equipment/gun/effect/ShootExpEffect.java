package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.zombies.equipment.gun.Gun;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ShootExpEffect implements Consumer<Gun> {

    private boolean currentlyActive = false;

    @Override
    public void accept(@NotNull Gun gun) {
        if (gun.getState().isMainEquipment()) {
            currentlyActive = true;
            if (gun.getState().ammo() > 0) {
                gun.getOwner().getPlayer().ifPresent(player -> {
                    float exp;
                    if (gun.getState().ammo() > 0) {
                        exp = (float) gun.getState().ticksSinceLastShot() / gun.getLevel().shootSpeed();
                    }
                    else {
                        exp = 0F;
                    }

                    player.setExp(exp);
                });
            }
        }
        else if (currentlyActive) {
            gun.getOwner().getPlayer().ifPresent(player -> {
                player.setExp(0);
            });
            currentlyActive = false;
        }
    }
}
