package org.phantazm.zombies.event.player;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.perk.effect.shot.ShotEffect;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

public class ZombiesPlayerIgniteMobEvent implements ZombiesPlayerEvent, CancellableEvent {
    private final Player player;
    private final ZombiesPlayer zombiesPlayer;
    private final ShotEffect cause;
    private final LivingEntity target;

    private boolean cancelled;

    public ZombiesPlayerIgniteMobEvent(@NotNull Player player, @NotNull ZombiesPlayer zombiesPlayer,
        @NotNull ShotEffect cause,
        @NotNull LivingEntity target) {
        this.player = Objects.requireNonNull(player);
        this.zombiesPlayer = Objects.requireNonNull(zombiesPlayer);
        this.cause = Objects.requireNonNull(cause);
        this.target = Objects.requireNonNull(target);
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

    public @NotNull ShotEffect cause() {
        return cause;
    }

    public @NotNull LivingEntity target() {
        return target;
    }
}
