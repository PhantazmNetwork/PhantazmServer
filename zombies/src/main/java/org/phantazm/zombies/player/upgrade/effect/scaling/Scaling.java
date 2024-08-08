package org.phantazm.zombies.player.upgrade.effect.scaling;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

public interface Scaling {
    double getMultiplier(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer, @NotNull TriggerData triggerData);
}
