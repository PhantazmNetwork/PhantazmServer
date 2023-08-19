package org.phantazm.zombies.listener;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Map;
import java.util.UUID;

public class PlayerInventoryPreClickListener extends ZombiesPlayerEventListener<InventoryPreClickEvent> {
    public PlayerInventoryPreClickListener(@NotNull Instance instance,
        @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers) {
        super(instance, zombiesPlayers);
    }

    @Override
    protected void accept(@NotNull ZombiesPlayer zombiesPlayer, @NotNull InventoryPreClickEvent event) {
        event.setCancelled(true);
    }
}
