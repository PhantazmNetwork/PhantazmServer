package org.phantazm.zombies.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

public class ZombiesPlayerDamageEvent implements ZombiesPlayerEvent, CancellableEvent {
    private final Player player;
    private final ZombiesPlayer zombiesPlayer;
    private final EntityDamageEvent damageCause;

    private boolean shouldKnock;
    private boolean shouldCancel;

    public ZombiesPlayerDamageEvent(@NotNull Player player, @NotNull ZombiesPlayer zombiesPlayer,
        @NotNull EntityDamageEvent damageCause) {
        this.player = Objects.requireNonNull(player);
        this.zombiesPlayer = Objects.requireNonNull(zombiesPlayer);
        this.damageCause = Objects.requireNonNull(damageCause);
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull ZombiesPlayer getZombiesPlayer() {
        return zombiesPlayer;
    }

    public void setShouldKnock(boolean shouldKnock) {
        this.shouldKnock = shouldKnock;
    }

    public boolean shouldKnock() {
        return shouldKnock;
    }

    public @NotNull EntityDamageEvent cause() {
        return damageCause;
    }

    @Override
    public boolean isCancelled() {
        return shouldCancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.shouldCancel = cancel;
    }
}
