package org.phantazm.zombies.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.handler.DoorHandler;
import org.phantazm.zombies.map.handler.ShopHandler;
import org.phantazm.zombies.map.shop.InteractionTypes;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerInteractBlockListener extends ZombiesPlayerEventListener<PlayerBlockInteractEvent> {

    private final ShopHandler shopHandler;

    private final DoorHandler doorHandler;

    private final PlayerRightClickListener rightClickListener;

    public PlayerInteractBlockListener(@NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers, @NotNull ShopHandler shopHandler,
            @NotNull DoorHandler doorHandler, @NotNull PlayerRightClickListener rightClickListener) {
        super(instance, zombiesPlayers);
        this.shopHandler = Objects.requireNonNull(shopHandler, "shopHandler");
        this.doorHandler = Objects.requireNonNull(doorHandler, "doorHandler");
        this.rightClickListener = Objects.requireNonNull(rightClickListener, "rightClickListener");
    }

    @Override
    public void accept(@NotNull ZombiesPlayer zombiesPlayer, @NotNull PlayerBlockInteractEvent event) {
        event.setCancelled(true);
        event.setBlockingItemUse(true);

        if (event.getHand() == Player.Hand.MAIN) {
            if (shopHandler.handleInteraction(zombiesPlayer, event.getBlockPosition(),
                    InteractionTypes.RIGHT_CLICK_BLOCK)) {
                return;
            }

            if (doorHandler.handleRightClick(zombiesPlayer, event.getBlockPosition())) {
                return;
            }

            rightClickListener.onRightClick(zombiesPlayer, event.getPlayer().getHeldSlot());
        }
    }
}
