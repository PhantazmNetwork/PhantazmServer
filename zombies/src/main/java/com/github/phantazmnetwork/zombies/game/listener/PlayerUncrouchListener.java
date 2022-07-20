package com.github.phantazmnetwork.zombies.game.listener;

import com.github.phantazmnetwork.zombies.game.event.ZombiesPlayerEvent;
import net.minestom.server.event.player.PlayerStopSneakingEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerUncrouchListener implements ZombiesPlayerEventListener<PlayerStopSneakingEvent> {
    @Override
    public void accept(@NotNull ZombiesPlayerEvent<PlayerStopSneakingEvent> event) {
        event.zombiesPlayer().setCrouching(false);
    }
}
