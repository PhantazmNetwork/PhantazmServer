package org.phantazm.zombies.player.upgrade.trigger;

import net.minestom.server.Tickable;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.effect.UpgradeEffect;

public interface UpgradeTrigger extends Tickable {
    void arm(@NotNull PlayerUpgrade upgrade, @NotNull UpgradeEffect effect);

    void disarm();

    @Override
    default void tick(long time) {
    }

    boolean needsTicking();
}
