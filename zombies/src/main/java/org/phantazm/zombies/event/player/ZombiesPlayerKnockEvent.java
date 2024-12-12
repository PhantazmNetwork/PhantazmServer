package org.phantazm.zombies.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.event.trait.DamageEvent;
import org.phantazm.zombies.event.trait.ZombiesPlayerEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

public class ZombiesPlayerKnockEvent implements ZombiesPlayerEvent, DamageEvent, CancellableEvent {
    private final Player player;
    private final ZombiesPlayer zombiesPlayer;
    private final Damage damage;

    private boolean cancelled;

    public ZombiesPlayerKnockEvent(@NotNull Player player, @NotNull ZombiesPlayer zombiesPlayer,
        @NotNull Damage damage) {
        this.player = Objects.requireNonNull(player);
        this.zombiesPlayer = Objects.requireNonNull(zombiesPlayer);
        this.damage = Objects.requireNonNull(damage);
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull ZombiesPlayer zombiesPlayer() {
        return zombiesPlayer;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull Damage damage() {
        return damage;
    }
}
