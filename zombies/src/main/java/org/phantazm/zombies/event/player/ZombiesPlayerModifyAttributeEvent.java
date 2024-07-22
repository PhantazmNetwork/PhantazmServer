package org.phantazm.zombies.event.player;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.perk.effect.shot.ShotEffect;
import org.phantazm.zombies.event.trait.LivingTargetEvent;
import org.phantazm.zombies.event.trait.ZombiesPlayerEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;
import java.util.UUID;

public class ZombiesPlayerModifyAttributeEvent implements ZombiesPlayerEvent, LivingTargetEvent, CancellableEvent {
    private final Player player;
    private final ZombiesPlayer zombiesPlayer;
    private final ShotEffect cause;
    private final LivingEntity target;
    private final UUID attributeUUID;
    private final Attribute attribute;

    private float attributeAmount;
    private boolean cancelled;

    public ZombiesPlayerModifyAttributeEvent(@NotNull Player player, @NotNull ZombiesPlayer zombiesPlayer,
        @NotNull ShotEffect cause,
        @NotNull LivingEntity target,
        @NotNull Attribute attribute,
        @NotNull UUID attributeUUID,
        float attributeAmount) {
        this.player = Objects.requireNonNull(player);
        this.zombiesPlayer = Objects.requireNonNull(zombiesPlayer);
        this.cause = Objects.requireNonNull(cause);
        this.target = Objects.requireNonNull(target);
        this.attributeUUID = Objects.requireNonNull(attributeUUID);
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
    public @NotNull ZombiesPlayer zombiesPlayer() {
        return zombiesPlayer;
    }

    public @NotNull LivingEntity target() {
        return target;
    }

    public @NotNull ShotEffect cause() {
        return cause;
    }

    public @NotNull UUID attributeUUID() {
        return attributeUUID;
    }

    public @NotNull Attribute attribute() {
        return attribute;
    }

    public float attributeAmount() {
        return attributeAmount;
    }

    public void setAttributeAmount(float amount) {
        this.attributeAmount = amount;
    }
}
