package org.phantazm.zombies.equipment.gun2.effect;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.equipment.gun2.GunModule;
import org.phantazm.zombies.equipment.gun2.GunState;
import org.phantazm.zombies.equipment.gun2.GunStats;
import org.phantazm.zombies.equipment.gun2.Keys;
import org.phantazm.zombies.equipment.gun2.reload.ReloadTester;
import org.phantazm.zombies.equipment.gun2.shoot.GunShoot;
import org.phantazm.zombies.equipment.gun2.shoot.ShootTester;
import org.phantazm.zombies.equipment.perk.effect.PerkEffect;
import org.phantazm.zombies.player.ZombiesPlayer;

public class GunTickEffect {

    public @NotNull PerkEffect forPlayer(@NotNull ZombiesPlayer zombiesPlayer, @NotNull InjectionStore injectionStore) {
        GunModule module = injectionStore.get(Keys.GUN_MODULE);
        return new Effect(module.shootTester(), module.reloadTester(), module.shoot(), module.stats(), module.state());
    }

    private static class Effect implements PerkEffect {

        private final ShootTester shootTester;

        private final ReloadTester reloadTester;

        private final GunShoot shoot;

        private final GunStats stats;

        private final GunState state;

        public Effect(ShootTester shootTester, ReloadTester reloadTester, GunShoot shoot, GunStats stats, GunState state) {
            this.shootTester = shootTester;
            this.reloadTester = reloadTester;
            this.shoot = shoot;
            this.stats = stats;
            this.state = state;
        }

        @Override
        public void tick(long time) {
            if (shootTester.isShooting()) {
                state.setTicksSinceLastShot(state.getTicksSinceLastShot() + 1);
            }
            if (shootTester.isFiring()) {
                state.setTicksSinceLastFire(state.getTicksSinceLastFire() + 1);
            }
            if (reloadTester.isReloading()) {
                state.setTicksSinceLastReload(state.getTicksSinceLastReload() + 1);
            } else if (!state.isReloadComplete()) {
                state.setClip(Math.min(stats.maxClip(), state.getAmmo()));
                state.setReloadComplete(true);
            }

            if (state.getQueuedShots() > 0) {
                if (shootTester.canFire()) {
                    shoot.fire();
                    state.setQueuedShots(state.getQueuedShots() - 1);
                } else if (!shootTester.isFiring()) {
                    state.setQueuedShots(0);
                }
            }
        }
    }

}
