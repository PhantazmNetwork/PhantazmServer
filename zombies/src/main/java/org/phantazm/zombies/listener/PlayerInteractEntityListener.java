package org.phantazm.zombies.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.map.handler.ShopHandler;
import org.phantazm.zombies.map.shop.InteractionTypes;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class PlayerInteractEntityListener extends ZombiesPlayerEventListener<PlayerEntityInteractEvent> {

    private final ShopHandler shopHandler;

    private final PlayerRightClickListener rightClickListener;

    public PlayerInteractEntityListener(@NotNull Instance instance,
        @NotNull Map<PlayerView, ZombiesPlayer> zombiesPlayers, @NotNull ShopHandler shopHandler,
        @NotNull PlayerRightClickListener rightClickListener, @NotNull Supplier<ZombiesScene> scene) {
        super(instance, zombiesPlayers, scene);
        this.shopHandler = Objects.requireNonNull(shopHandler);
        this.rightClickListener = Objects.requireNonNull(rightClickListener);
    }

    @Override
    public void accept(@NotNull ZombiesScene scene, @NotNull ZombiesPlayer zombiesPlayer, @NotNull PlayerEntityInteractEvent event) {
        if (event.getHand() != Player.Hand.MAIN) {
            return;
        }

        if (shopHandler.handleInteraction(zombiesPlayer, event.getTarget().getPosition(),
            InteractionTypes.RIGHT_CLICK_ENTITY)) {
            return;
        }

        rightClickListener.onRightClick(zombiesPlayer, event.getPlayer().getHeldSlot());
    }
}
