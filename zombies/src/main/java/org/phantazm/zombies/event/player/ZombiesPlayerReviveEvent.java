package org.phantazm.zombies.event.player;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.event.trait.ZombiesPlayerEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

public class ZombiesPlayerReviveEvent implements ZombiesPlayerEvent {
    private final Player player;
    private final ZombiesPlayer zombiesPlayer;
    private final ZombiesPlayer reviveTarget;

    public ZombiesPlayerReviveEvent(@NotNull Player player, @NotNull ZombiesPlayer zombiesPlayer,
        @NotNull ZombiesPlayer reviveTarget) {
        this.player = Objects.requireNonNull(player);
        this.zombiesPlayer = Objects.requireNonNull(zombiesPlayer);
        this.reviveTarget = Objects.requireNonNull(reviveTarget);
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull ZombiesPlayer zombiesPlayer() {
        return zombiesPlayer;
    }

    public @NotNull ZombiesPlayer reviveTarget() {
        return reviveTarget;
    }
}
