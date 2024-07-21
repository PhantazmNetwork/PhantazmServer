package org.phantazm.zombies.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

public class ZombiesPlayerKillMobEvent implements ZombiesPlayerEvent {
    private final Player player;
    private final ZombiesPlayer zombiesPlayer;
    private final Mob mob;
    private final Damage lastDamageSource;

    public ZombiesPlayerKillMobEvent(@NotNull Player player, @NotNull ZombiesPlayer zombiesPlayer, @NotNull Mob mob,
        @NotNull Damage lastDamageSource) {
        this.player = Objects.requireNonNull(player);
        this.zombiesPlayer = Objects.requireNonNull(zombiesPlayer);
        this.mob = mob;
        this.lastDamageSource = Objects.requireNonNull(lastDamageSource);
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull ZombiesPlayer getZombiesPlayer() {
        return zombiesPlayer;
    }

    public @NotNull Mob target() {
        return mob;
    }

    public @NotNull Damage lastDamageSource() {
        return lastDamageSource;
    }
}
