package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.zombies.equipment.gun.Gun;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public abstract class ReloadActionBarEffect implements GunEffect {

    private boolean active = false;

    @Override
    public void accept(@NotNull Gun gun) {
        if (gun.isReloading()) {
            if (gun.getState().isMainEquipment()) {
                float progress = (float) gun.getState().ticksSinceLastReload() / gun.getLevel().reloadSpeed();
                gun.getOwner().getPlayer().ifPresent(player -> {
                    player.sendActionBar(getComponent(progress));
                });
            }
            active = true;
        } else if (active) {
            if (gun.getState().isMainEquipment()) {
                gun.getOwner().getPlayer().ifPresent(player -> {
                    player.sendActionBar(Component.empty());
                });
            }
            active = false;
        }
    }

    @Override
    public void tick(long time) {

    }

    protected abstract @NotNull Component getComponent(float progress);

}
