package org.phantazm.zombies.player.upgrade.selector;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.Target;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

public interface Selector {
    @NotNull
    Target select(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
        @NotNull TriggerData triggerData);
}
