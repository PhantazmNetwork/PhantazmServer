package org.phantazm.zombies.event.player;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

public class ZombiesPlayerDamageEvent implements ZombiesPlayerEvent {
    private final Player player;
    private final ZombiesPlayer zombiesPlayer;

    private boolean shouldDie;

    public ZombiesPlayerDamageEvent(@NotNull Player player, @NotNull ZombiesPlayer zombiesPlayer) {
        this.player = Objects.requireNonNull(player);
        this.zombiesPlayer = Objects.requireNonNull(zombiesPlayer);
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull ZombiesPlayer getZombiesPlayer() {
        return zombiesPlayer;
    }

    public void setShouldDie(boolean shouldDie) {
        this.shouldDie = shouldDie;
    }

    public boolean shouldDie() {
        return shouldDie;
    }
}
