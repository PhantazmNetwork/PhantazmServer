package com.github.phantazmnetwork.zombies.listener;

import com.github.phantazmnetwork.zombies.map.shop.InteractionTypes;
import com.github.phantazmnetwork.zombies.map.handler.ShopHandler;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerUseItemOnBlockListener extends ZombiesPlayerEventListener<PlayerUseItemOnBlockEvent> {

    private final ShopHandler shopHandler;

    private final PlayerRightClickListener rightClickListener;

    public PlayerUseItemOnBlockListener(@NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers, @NotNull ShopHandler shopHandler,
            @NotNull PlayerRightClickListener rightClickListener) {
        super(instance, zombiesPlayers);
        this.shopHandler = Objects.requireNonNull(shopHandler, "shopHandler");
        this.rightClickListener = Objects.requireNonNull(rightClickListener, "rightClickListener");
    }

    @Override
    public void accept(@NotNull ZombiesPlayer zombiesPlayer, @NotNull PlayerUseItemOnBlockEvent event) {
        if (event.getHand() == Player.Hand.MAIN) {
            shopHandler.handleInteraction(zombiesPlayer, event.getPosition(), InteractionTypes.RIGHT_CLICK_BLOCK);
            rightClickListener.onRightClick(zombiesPlayer, event.getPlayer().getHeldSlot());
        }
    }
}
