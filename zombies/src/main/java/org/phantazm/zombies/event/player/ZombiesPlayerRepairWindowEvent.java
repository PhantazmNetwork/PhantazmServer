package org.phantazm.zombies.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Window;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

public class ZombiesPlayerRepairWindowEvent implements ZombiesPlayerEvent, CancellableEvent {
    private final Player player;
    private final ZombiesPlayer zombiesPlayer;
    private final Window window;
    private final int amount;

    private int goldGain;
    private boolean cancelled;

    public ZombiesPlayerRepairWindowEvent(@NotNull Player player, @NotNull ZombiesPlayer zombiesPlayer,
        @NotNull Window window, int amount, int goldGain) {
        this.player = Objects.requireNonNull(player);
        this.zombiesPlayer = Objects.requireNonNull(zombiesPlayer);
        this.window = Objects.requireNonNull(window);
        this.amount = amount;
        this.goldGain = goldGain;
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

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public int goldGain() {
        return this.goldGain;
    }

    public void setGoldGain(int newGain) {
        this.goldGain = newGain;
    }
}
