package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.Gun;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public class AmmoLevelEffect implements GunEffect {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.effect.level.ammo");

    private boolean currentlyActive = false;

    @Override
    public void accept(@NotNull Gun gun) {
        if (gun.getState().isMainEquipment()) {
            currentlyActive = true;
            gun.getOwner().getPlayer().ifPresent(player -> {
                player.setLevel(gun.getState().ammo());
            });
        }
        else if (currentlyActive) {
            gun.getOwner().getPlayer().ifPresent(player -> {
                player.setLevel(0);
            });
            currentlyActive = false;
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
