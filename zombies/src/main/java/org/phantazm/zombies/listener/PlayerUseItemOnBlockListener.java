package org.phantazm.zombies.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.handler.ShopHandler;
import org.phantazm.zombies.map.shop.InteractionTypes;
import org.phantazm.zombies.player.ZombiesPlayer;

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
