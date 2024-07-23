package org.phantazm.zombies.event.player;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.event.trait.SettableDamageAmountEvent;
import org.phantazm.zombies.event.trait.LivingTargetEvent;
import org.phantazm.zombies.event.trait.ZombiesPlayerEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

public class ZombiesPlayerProcFireEvent implements ZombiesPlayerEvent, LivingTargetEvent, SettableDamageAmountEvent,
    CancellableEvent {
    private final Player player;
    private final ZombiesPlayer zombiesPlayer;
    private final LivingEntity target;
    private float amount;

    private boolean cancelled;

    public ZombiesPlayerProcFireEvent(@NotNull Player player, @NotNull ZombiesPlayer zombiesPlayer,
        @NotNull LivingEntity target, float amount) {
        this.player = Objects.requireNonNull(player);
        this.zombiesPlayer = Objects.requireNonNull(zombiesPlayer);
        this.target = Objects.requireNonNull(target);
        this.amount = amount;
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

    public @NotNull LivingEntity target() {
        return target;
    }

    @Override
    public float damageAmount() {
        return amount;
    }

    @Override
    public void setDamageAmount(float damage) {
        this.amount = damage;
    }
}
