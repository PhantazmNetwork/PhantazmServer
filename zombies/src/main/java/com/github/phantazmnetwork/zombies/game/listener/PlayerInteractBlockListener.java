package com.github.phantazmnetwork.zombies.game.listener;

import com.github.phantazmnetwork.zombies.game.event.ZombiesPlayerEvent;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PlayerInteractBlockListener implements ZombiesPlayerEventListener<PlayerBlockInteractEvent> {

    private final PlayerRightClickListener rightClickListener;

    public PlayerInteractBlockListener(@NotNull PlayerRightClickListener rightClickListener) {
        this.rightClickListener = Objects.requireNonNull(rightClickListener, "rightClickListener");
    }

    @Override
    public void accept(@NotNull ZombiesPlayerEvent<PlayerBlockInteractEvent> event) {
        PlayerBlockInteractEvent blockInteractEvent = event.event();
        if (blockInteractEvent.getHand() == Player.Hand.MAIN) {
            rightClickListener.onRightClick(event.zombiesPlayer(), event.event().getPlayer().getHeldSlot());
        }
    }
}
