package org.phantazm.zombies.event.player;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.event.trait.ZombiesPlayerEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

public class ZombiesPlayerModifyUpgrade implements ZombiesPlayerEvent {
    private final Player player;
    private final ZombiesPlayer zombiesPlayer;
    private final Key upgradeKey;
    private final boolean added;

    public ZombiesPlayerModifyUpgrade(@NotNull Player player, @NotNull ZombiesPlayer zombiesPlayer,
        @NotNull Key upgradeKey, boolean added) {
        this.player = player;
        this.zombiesPlayer = zombiesPlayer;
        this.upgradeKey = upgradeKey;
        this.added = added;
    }
    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull ZombiesPlayer zombiesPlayer() {
        return zombiesPlayer;
    }

    public @NotNull Key upgradeKey() {
        return upgradeKey;
    }

    public boolean added() {
        return added;
    }
}
