package com.github.phantazmnetwork.zombies.game.listener;

import com.github.phantazmnetwork.zombies.game.event.ZombiesPlayerEvent;
import net.minestom.server.event.player.PlayerStartSneakingEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerCrouchListener implements ZombiesPlayerEventListener<PlayerStartSneakingEvent> {

    @Override
    public void accept(@NotNull ZombiesPlayerEvent<PlayerStartSneakingEvent> event) {
        event.zombiesPlayer().setCrouching(true);
    }
}
