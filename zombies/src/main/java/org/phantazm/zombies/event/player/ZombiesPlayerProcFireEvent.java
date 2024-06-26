package org.phantazm.zombies.event.player;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

public class ZombiesPlayerProcFireEvent implements ZombiesPlayerEvent, CancellableEvent {
    private final Player player;
    private final ZombiesPlayer zombiesPlayer;
    private final LivingEntity target;
    private float amount;

    private boolean cancelled;

    public ZombiesPlayerProcFireEvent(@NotNull Player player, @NotNull ZombiesPlayer zombiesPlayer, float amount,
        @NotNull LivingEntity target) {
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
    public @NotNull ZombiesPlayer getZombiesPlayer() {
        return zombiesPlayer;
    }

    public float damageAmount() {
        return amount;
    }

    public void setDamageAmount(float amount) {
        this.amount = amount;
    }

    public @NotNull LivingEntity target() {
        return target;
    }
}
