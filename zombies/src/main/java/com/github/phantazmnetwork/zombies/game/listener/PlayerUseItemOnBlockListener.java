package com.github.phantazmnetwork.zombies.game.listener;

import com.github.phantazmnetwork.zombies.game.event.ZombiesPlayerEvent;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PlayerUseItemOnBlockListener implements ZombiesPlayerEventListener<PlayerUseItemOnBlockEvent> {

    private final PlayerRightClickListener rightClickListener;

    public PlayerUseItemOnBlockListener(@NotNull PlayerRightClickListener rightClickListener) {
        this.rightClickListener = Objects.requireNonNull(rightClickListener, "rightClickListener");
    }

    @Override
    public void accept(@NotNull ZombiesPlayerEvent<PlayerUseItemOnBlockEvent> event) {
        PlayerUseItemOnBlockEvent useItemEvent = event.event();
        if (useItemEvent.getHand() == Player.Hand.MAIN) {
            rightClickListener.onRightClick(event.zombiesPlayer(), event.event().getPlayer().getHeldSlot());
        }
    }
}
