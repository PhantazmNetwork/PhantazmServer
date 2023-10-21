package org.phantazm.zombies.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.map.handler.DoorHandler;
import org.phantazm.zombies.map.handler.ShopHandler;
import org.phantazm.zombies.map.shop.InteractionTypes;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class PlayerInteractBlockListener extends ZombiesPlayerEventListener<PlayerBlockInteractEvent> {

    private final ShopHandler shopHandler;

    private final DoorHandler doorHandler;

    private final PlayerRightClickListener rightClickListener;

    public PlayerInteractBlockListener(@NotNull Instance instance,
        @NotNull Map<PlayerView, ZombiesPlayer> zombiesPlayers, @NotNull ShopHandler shopHandler,
        @NotNull DoorHandler doorHandler, @NotNull PlayerRightClickListener rightClickListener,
        @NotNull Supplier<ZombiesScene> scene) {
        super(instance, zombiesPlayers, scene);
        this.shopHandler = Objects.requireNonNull(shopHandler);
        this.doorHandler = Objects.requireNonNull(doorHandler);
        this.rightClickListener = Objects.requireNonNull(rightClickListener);
    }

    @Override
    public void accept(@NotNull ZombiesScene scene, @NotNull ZombiesPlayer zombiesPlayer, @NotNull PlayerBlockInteractEvent event) {
        event.setCancelled(true);

        if (event.getBlock().registry().material() != Material.CHEST) {
            event.setBlockingItemUse(true);
        }

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
