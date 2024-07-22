package org.phantazm.zombies.event.trait;

import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

public interface ZombiesPlayerEvent extends PlayerEvent {
    @NotNull ZombiesPlayer zombiesPlayer();
}
