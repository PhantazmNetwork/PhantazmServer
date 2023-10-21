package org.phantazm.zombies.listener;

import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Map;
import java.util.function.Supplier;

public class PlayerDropItemListener extends ZombiesPlayerEventListener<ItemDropEvent> {

    public PlayerDropItemListener(@NotNull Instance instance,
        @NotNull Map<PlayerView, ZombiesPlayer> zombiesPlayers, @NotNull Supplier<ZombiesScene> scene) {
        super(instance, zombiesPlayers, scene);
    }

    @Override
    protected void accept(@NotNull ZombiesScene scene, @NotNull ZombiesPlayer zombiesPlayer, @NotNull ItemDropEvent event) {
        event.setCancelled(true);
    }
}
