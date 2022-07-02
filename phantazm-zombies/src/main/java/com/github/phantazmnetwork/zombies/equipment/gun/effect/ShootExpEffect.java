package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.Gun;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public class ShootExpEffect implements GunEffect {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.effect.exp.shoot");

    private boolean currentlyActive = false;

    @Override
    public void accept(@NotNull Gun gun) {
        if (gun.getState().isMainEquipment()) {
            currentlyActive = true;
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
        else if (currentlyActive) {
            gun.getOwner().getPlayer().ifPresent(player -> {
                player.setExp(0);
            });
            currentlyActive = false;
        }
    }

    @Override
    public @NotNull Key getSerialKey() {
        return SERIAL_KEY;
    }
}
