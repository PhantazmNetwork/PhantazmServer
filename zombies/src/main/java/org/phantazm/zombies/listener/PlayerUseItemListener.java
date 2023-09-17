package org.phantazm.zombies.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class PlayerUseItemListener extends ZombiesPlayerEventListener<PlayerUseItemEvent> {

    private final PlayerRightClickListener rightClickListener;

    public PlayerUseItemListener(@NotNull Instance instance,
        @NotNull Map<PlayerView, ZombiesPlayer> zombiesPlayers,
        @NotNull PlayerRightClickListener rightClickListener, @NotNull Supplier<ZombiesScene> scene) {
        super(instance, zombiesPlayers, scene);
        this.rightClickListener = Objects.requireNonNull(rightClickListener);
    }

    @Override
    public void accept(@NotNull ZombiesScene scene, @NotNull ZombiesPlayer zombiesPlayer, @NotNull PlayerUseItemEvent event) {
        if (event.getHand() == Player.Hand.MAIN) {
            rightClickListener.onRightClick(zombiesPlayer, event.getPlayer().getHeldSlot());
        }
    }
}
