package org.phantazm.core.event.equipment;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.core.inventory.InventoryAccessRegistry;

import java.util.Objects;

public class EquipmentPreAddEvent implements EquipmentEvent, CancellableEvent {
    private final Player player;
    private final Equipment equipment;
    private final Key groupKey;
    private final InventoryAccessRegistry accessRegistry;

    private boolean cancelled;

    public EquipmentPreAddEvent(@NotNull Player player, @NotNull Equipment equipment, @NotNull Key groupKey,
        @NotNull InventoryAccessRegistry accessRegistry) {
        this.player = Objects.requireNonNull(player);
        this.equipment = Objects.requireNonNull(equipment);
        this.groupKey = Objects.requireNonNull(groupKey);
        this.accessRegistry = Objects.requireNonNull(accessRegistry);
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

    public @NotNull Key groupKey() {
        return groupKey;
    }

    public @NotNull InventoryAccessRegistry accessRegistry() {
        return accessRegistry;
    }
}
