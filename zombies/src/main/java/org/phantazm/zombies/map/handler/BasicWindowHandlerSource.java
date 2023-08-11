package org.phantazm.zombies.map.handler;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.tracker.BoundedTracker;
import org.phantazm.mob.MobStore;
import org.phantazm.zombies.map.Room;
import org.phantazm.zombies.map.Window;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Collection;
import java.util.Objects;

public class BasicWindowHandlerSource implements WindowHandler.Source {
    private final double repairRadius;
    private final long repairInterval;
    private final int coinsPerWindowBlock;
    private final WindowHandler.WindowMessages windowMessages;

    public BasicWindowHandlerSource(double repairRadius, long repairInterval, int coinsPerWindowBlock,
            @NotNull WindowHandler.WindowMessages windowMessages) {
        this.repairRadius = repairRadius;
        this.repairInterval = repairInterval;
        this.coinsPerWindowBlock = coinsPerWindowBlock;
        this.windowMessages = Objects.requireNonNull(windowMessages, "windowMessages");
    }

    @Override
    public @NotNull WindowHandler make(@NotNull BoundedTracker<Window> windowTracker,
            @NotNull BoundedTracker<Room> roomTracker, @NotNull MobStore mobStore,
            @NotNull Collection<? extends ZombiesPlayer> players) {
        return new BasicWindowHandler(windowTracker, roomTracker, mobStore, players, repairRadius, repairInterval,
                coinsPerWindowBlock, windowMessages);
    }
}
