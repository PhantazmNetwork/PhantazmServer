package org.phantazm.zombies.equipment.gun2.interactor;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.equipment.gun2.GunModule;
import org.phantazm.zombies.equipment.gun2.GunState;
import org.phantazm.zombies.equipment.gun2.Keys;
import org.phantazm.zombies.equipment.gun2.reload.GunReload;
import org.phantazm.zombies.equipment.gun2.shoot.GunShoot;
import org.phantazm.zombies.equipment.perk.equipment.interactor.PerkInteractor;
import org.phantazm.zombies.equipment.perk.equipment.interactor.PerkInteractorCreator;
import org.phantazm.zombies.player.ZombiesPlayer;

public class GunInteractorCreator implements PerkInteractorCreator {

    @Override
    public @NotNull PerkInteractor forPlayer(@NotNull ZombiesPlayer zombiesPlayer, @NotNull InjectionStore injectionStore) {
        GunModule module = injectionStore.get(Keys.GUN_MODULE);
        return new Interactor(module.state(), module.shoot(), module.reload());
    }

    private static class Interactor implements PerkInteractor {

        private final GunState state;

        private final GunShoot shoot;

        private final GunReload reload;

        public Interactor(GunState state, @NotNull GunShoot shoot, @NotNull GunReload reload) {
            this.state = state;
            this.shoot = shoot;
            this.reload = reload;
        }

        @Override
        public boolean setSelected(boolean selected) {
            state.setHeld(selected);
            return true;
        }

        @Override
        public boolean leftClick() {
            reload.reload();
            return true;
        }

        @Override
        public boolean rightClick() {
            shoot.shoot();
            return true;
        }

        @Override
        public boolean attack(@NotNull Entity target) {
            return false;
        }

    }

}
