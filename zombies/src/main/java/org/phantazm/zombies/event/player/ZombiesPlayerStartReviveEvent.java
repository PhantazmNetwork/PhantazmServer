package org.phantazm.zombies.event.player;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.event.trait.EntityTargetEvent;
import org.phantazm.zombies.event.trait.ZombiesPlayerEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

public class ZombiesPlayerStartReviveEvent implements ZombiesPlayerEvent, EntityTargetEvent {
    private final Player reviverPlayer;
    private final ZombiesPlayer zombiesPlayer;
    private final Player reviveTargetPlayer;
    private final ZombiesPlayer reviveTarget;

    public ZombiesPlayerStartReviveEvent(@NotNull Player reviverPlayer, @NotNull ZombiesPlayer reviver,
        @NotNull Player reviveePlayer,
        @NotNull ZombiesPlayer revivee) {
        this.reviverPlayer = Objects.requireNonNull(reviverPlayer);
        this.zombiesPlayer = Objects.requireNonNull(reviver);
        this.reviveTargetPlayer = Objects.requireNonNull(reviveePlayer);
        this.reviveTarget = Objects.requireNonNull(revivee);
    }

    @Override
    public @NotNull Player getPlayer() {
        return reviverPlayer;
    }

    @Override
    public @NotNull Player getEntity() {
        return reviverPlayer;
    }

    @Override
    public @NotNull ZombiesPlayer zombiesPlayer() {
        return zombiesPlayer;
    }

    @Override
    public @NotNull Entity target() {
        return reviveTargetPlayer;
    }

    public @NotNull ZombiesPlayer reviveTarget() {
        return reviveTarget;
    }
}
