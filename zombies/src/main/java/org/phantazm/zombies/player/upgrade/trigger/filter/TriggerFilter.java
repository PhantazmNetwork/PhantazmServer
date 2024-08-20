package org.phantazm.zombies.player.upgrade.trigger.filter;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

public interface TriggerFilter {
    TriggerFilter NONE = (upgrade, player, data) -> false;
    TriggerFilter ALL = (upgrade, player, data) -> true;

    boolean test(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer, @NotNull TriggerData triggerData);
}
