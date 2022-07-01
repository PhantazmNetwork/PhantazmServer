package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.zombies.equipment.gun.Gun;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class AmmoLevelEffect implements Consumer<Gun> {

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
}
