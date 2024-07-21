package org.phantazm.zombies.player.upgrade.trigger;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.effect.UpgradeEffect;

public interface UpgradeTrigger {
    UpgradeTrigger NONE = new UpgradeTrigger() {
        @Override
        public void arm(@NotNull PlayerUpgrade upgrade, @NotNull UpgradeEffect effect) {

        }

        @Override
        public void disarm() {

        }
    };

    void arm(@NotNull PlayerUpgrade upgrade, @NotNull UpgradeEffect effect);

    void disarm();
}
