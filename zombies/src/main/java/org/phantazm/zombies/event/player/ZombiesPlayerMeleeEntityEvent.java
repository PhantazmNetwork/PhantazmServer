package org.phantazm.zombies.event.player;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.zombies.event.trait.EquipmentEvent;
import org.phantazm.zombies.event.trait.LivingTargetEvent;
import org.phantazm.zombies.event.trait.ZombiesPlayerEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

public class ZombiesPlayerMeleeEntityEvent implements ZombiesPlayerEvent, EquipmentEvent, LivingTargetEvent {
    private final Player player;
    private final ZombiesPlayer zombiesPlayer;
    private final LivingEntity mob;
    private final Equipment equipment;

    public ZombiesPlayerMeleeEntityEvent(@NotNull Player player, @NotNull ZombiesPlayer zombiesPlayer, @NotNull LivingEntity mob,
        @NotNull Equipment equipment) {
        this.player = Objects.requireNonNull(player);
        this.zombiesPlayer = Objects.requireNonNull(zombiesPlayer);
        this.mob = mob;
        this.equipment = Objects.requireNonNull(equipment);
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull Player getEntity() {
        return player;
    }

    @Override
    public @NotNull ZombiesPlayer zombiesPlayer() {
        return zombiesPlayer;
    }

    public @NotNull Equipment equipment() {
        return equipment;
    }

    public @NotNull LivingEntity target() {
        return mob;
    }
}
