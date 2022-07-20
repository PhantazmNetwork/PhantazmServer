package com.github.phantazmnetwork.zombies.game.listener;

import com.github.phantazmnetwork.zombies.game.event.ZombiesPlayerEvent;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PlayerInteractEntityListener implements ZombiesPlayerEventListener<PlayerEntityInteractEvent> {

    private final PlayerRightClickListener rightClickListener;

    public PlayerInteractEntityListener(@NotNull PlayerRightClickListener rightClickListener) {
        this.rightClickListener = Objects.requireNonNull(rightClickListener, "rightClickListener");
    }

    @Override
    public void accept(@NotNull ZombiesPlayerEvent<PlayerEntityInteractEvent> event) {
        PlayerEntityInteractEvent entityInteractEvent = event.event();
        if (entityInteractEvent.getHand() == Player.Hand.MAIN) {
            rightClickListener.onRightClick(event.zombiesPlayer(), entityInteractEvent.getPlayer().getHeldSlot());
        }
    }
}
