package org.phantazm.zombies.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Door;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

public class OpenDoorEvent implements ZombiesPlayerEvent, CancellableEvent {
    private final Player player;
    private final ZombiesPlayer zombiesPlayer;
    private final Door door;

    private boolean cancelled;

    public OpenDoorEvent(@NotNull Player player, @NotNull ZombiesPlayer zombiesPlayer, @NotNull Door door) {
        this.player = Objects.requireNonNull(player);
        this.zombiesPlayer = Objects.requireNonNull(zombiesPlayer);
        this.door = Objects.requireNonNull(door);
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull ZombiesPlayer getZombiesPlayer() {
        return zombiesPlayer;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public @NotNull Door door() {
        return door;
    }
}
