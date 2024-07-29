package org.phantazm.zombies.player.upgrade.validator;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

public interface Validator {
    boolean test(@NotNull Entity candidate, @NotNull PlayerUpgrade playerUpgrade, @NotNull ZombiesPlayer zombiesPlayer, @NotNull TriggerData data);
}
