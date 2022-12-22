package org.phantazm.zombies.map.handler;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Window;

import java.util.List;

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
    public @NotNull WindowHandler make(@NotNull List<Window> windows) {
        return new BasicWindowHandler(windows, repairRadius, repairInterval, coinsPerWindowBlock);
    }
}
