package org.phantazm.core.event.equipment;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.Equipment;

import java.util.Objects;

public class EquipmentAddEvent implements EquipmentEvent, CancellableEvent {
    private final Player player;
    private final Equipment equipment;

    private boolean cancelled;

    public EquipmentAddEvent(@NotNull Player player, @NotNull Equipment equipment) {
        this.player = Objects.requireNonNull(player);
        this.equipment = Objects.requireNonNull(equipment);
    }

    @Override
    public @NotNull Equipment equipment() {
        return equipment;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
