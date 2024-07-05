package org.phantazm.zombies.event.player;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

public class PlayerReviveEvent implements ZombiesPlayerEvent {
    private final Player player;
    private final ZombiesPlayer zombiesPlayer;
    private final ZombiesPlayer reviveTarget;

    public PlayerReviveEvent(@NotNull Player player, @NotNull ZombiesPlayer zombiesPlayer, @NotNull ZombiesPlayer reviveTarget) {
        this.player = Objects.requireNonNull(player);
        this.zombiesPlayer = Objects.requireNonNull(zombiesPlayer);
        this.reviveTarget = Objects.requireNonNull(reviveTarget);
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull ZombiesPlayer reviveTarget() {
        return reviveTarget;
    }

    @Override
    public @NotNull ZombiesPlayer getZombiesPlayer() {
        return zombiesPlayer;
    }
}
