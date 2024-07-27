package org.phantazm.zombies.event.player;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.event.trait.SettableAttributeEvent;
import org.phantazm.zombies.event.trait.ZombiesPlayerEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;
import java.util.UUID;

public class ZombiesPlayerModifyAttributeEvent implements ZombiesPlayerEvent, SettableAttributeEvent, CancellableEvent {
    private final Player player;
    private final ZombiesPlayer zombiesPlayer;
    private final LivingEntity target;
    private final UUID attributeUuid;
    private final Attribute attribute;

    private double attributeAmount;
    private boolean cancelled;

    public ZombiesPlayerModifyAttributeEvent(@NotNull Player player, @NotNull ZombiesPlayer zombiesPlayer,
        @NotNull LivingEntity target,
        @NotNull Attribute attribute,
        @NotNull UUID attributeUuid,
        double attributeAmount) {
        this.player = Objects.requireNonNull(player);
        this.zombiesPlayer = Objects.requireNonNull(zombiesPlayer);
        this.target = Objects.requireNonNull(target);
        this.attributeUuid = Objects.requireNonNull(attributeUuid);
        this.attribute = Objects.requireNonNull(attribute);
        this.attributeAmount = attributeAmount;
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

    @Override
    public @NotNull Player getEntity() {
        return player;
    }

    @Override
    public @NotNull ZombiesPlayer zombiesPlayer() {
        return zombiesPlayer;
    }

    public @NotNull UUID attributeUuid() {
        return attributeUuid;
    }

    public @NotNull Attribute attribute() {
        return attribute;
    }

    public double attributeAmount() {
        return attributeAmount;
    }

    @Override
    public boolean isRemove() {
        return false;
    }

    public void setAttributeAmount(float amount) {
        this.attributeAmount = amount;
    }

    @Override
    public @NotNull LivingEntity target() {
        return target;
    }
}
