package org.phantazm.zombies.map.handler;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Window;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class BasicWindowHandlerSource implements WindowHandler.Source {
    private final double repairRadius;
    private final long repairInterval;
    private final int coinsPerWindowBlock;

    public BasicWindowHandlerSource(double repairRadius, long repairInterval, int coinsPerWindowBlock) {
        this.repairRadius = repairRadius;
        this.repairInterval = repairInterval;
        this.coinsPerWindowBlock = coinsPerWindowBlock;
    }

    @Override
    public @NotNull WindowHandler make(@NotNull MapObjects mapObjects,
            @NotNull Collection<? extends ZombiesPlayer> players) {
        return new BasicWindowHandler(mapObjects, players, repairRadius, repairInterval, coinsPerWindowBlock);
    }
}
