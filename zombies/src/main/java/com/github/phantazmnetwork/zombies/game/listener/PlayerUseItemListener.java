package com.github.phantazmnetwork.zombies.game.listener;

import com.github.phantazmnetwork.zombies.game.event.ZombiesPlayerEvent;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerUseItemEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PlayerUseItemListener implements ZombiesPlayerEventListener<PlayerUseItemEvent> {

    private final PlayerRightClickListener rightClickListener;

    public PlayerUseItemListener(@NotNull PlayerRightClickListener rightClickListener) {
        this.rightClickListener = Objects.requireNonNull(rightClickListener, "rightClickListener");
    }

    @Override
    public void accept(@NotNull ZombiesPlayerEvent<PlayerUseItemEvent> event) {
        PlayerUseItemEvent useItemEvent = event.event();
        if (useItemEvent.getHand() == Player.Hand.MAIN) {
            rightClickListener.onRightClick(event.zombiesPlayer(), useItemEvent.getPlayer().getHeldSlot());
        }
    }
}
