package org.phantazm.zombies.event;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

public class ZombiesPlayerDeathEvent implements ZombiesPlayerEvent {
    private final Player player;
    private final ZombiesPlayer zombiesPlayer;
    private final DamageType damageType;

    private boolean cancelled;

    public ZombiesPlayerDeathEvent(@NotNull Player player, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull DamageType damageType) {
        this.player = Objects.requireNonNull(player, "player");
        this.zombiesPlayer = Objects.requireNonNull(zombiesPlayer, "zombiesPlayer");
        this.damageType = Objects.requireNonNull(damageType, "damageType");
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull ZombiesPlayer getZombiesPlayer() {
        return zombiesPlayer;
    }

    public @NotNull DamageType damageType() {
        return damageType;
    }

    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }
}
