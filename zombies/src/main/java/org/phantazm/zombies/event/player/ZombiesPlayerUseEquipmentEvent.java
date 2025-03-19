package org.phantazm.zombies.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.zombies.event.trait.EquipmentEvent;
import org.phantazm.zombies.event.trait.ZombiesPlayerEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

public class ZombiesPlayerUseEquipmentEvent implements ZombiesPlayerEvent, EquipmentEvent, CancellableEvent {
    private final ZombiesPlayer zombiesPlayer;
    private final Player player;
    private final Equipment equipment;

    private boolean isCancelled;

    public ZombiesPlayerUseEquipmentEvent(@NotNull ZombiesPlayer zombiesPlayer, @NotNull Player player, @NotNull Equipment equipment) {
        this.zombiesPlayer = zombiesPlayer;
        this.player = player;
        this.equipment = equipment;
    }

    @Override
    public @NotNull ZombiesPlayer zombiesPlayer() {
        return zombiesPlayer;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull Equipment equipment() {
        return equipment;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }
}
