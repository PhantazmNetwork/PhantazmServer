package org.phantazm.zombies.event;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Window;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

public class PlayerRepairWindowEvent implements ZombiesPlayerEvent {
    private final Player player;
    private final ZombiesPlayer zombiesPlayer;
    private final Window window;
    private final int amount;

    public PlayerRepairWindowEvent(@NotNull Player player, @NotNull ZombiesPlayer zombiesPlayer, @NotNull Window window,
            int amount) {
        this.player = Objects.requireNonNull(player, "player");
        this.zombiesPlayer = Objects.requireNonNull(zombiesPlayer, "zombiesPlayer");
        this.window = Objects.requireNonNull(window, "window");
        this.amount = amount;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull ZombiesPlayer getZombiesPlayer() {
        return zombiesPlayer;
    }

    public @NotNull Window getWindow() {
        return window;
    }

    public int getAmount() {
        return amount;
    }
}
