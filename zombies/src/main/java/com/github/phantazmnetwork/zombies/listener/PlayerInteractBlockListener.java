package com.github.phantazmnetwork.zombies.listener;

import com.github.phantazmnetwork.zombies.map.shop.InteractionTypes;
import com.github.phantazmnetwork.zombies.map.handler.ShopHandler;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerInteractBlockListener extends ZombiesPlayerEventListener<PlayerBlockInteractEvent> {

    private final ShopHandler shopHandler;

    private final PlayerRightClickListener rightClickListener;

    public PlayerInteractBlockListener(@NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers, @NotNull ShopHandler shopHandler,
            @NotNull PlayerRightClickListener rightClickListener) {
        super(instance, zombiesPlayers);
        this.shopHandler = Objects.requireNonNull(shopHandler, "shopHandler");
        this.rightClickListener = Objects.requireNonNull(rightClickListener, "rightClickListener");
    }

    @Override
    public void accept(@NotNull ZombiesPlayer zombiesPlayer, @NotNull PlayerBlockInteractEvent event) {
        if (event.getHand() == Player.Hand.MAIN) {
            shopHandler.handleInteraction(zombiesPlayer, event.getBlockPosition(), InteractionTypes.RIGHT_CLICK_BLOCK);
            rightClickListener.onRightClick(zombiesPlayer, event.getPlayer().getHeldSlot());
        }
    }
}
