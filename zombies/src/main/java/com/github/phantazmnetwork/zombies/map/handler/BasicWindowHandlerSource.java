package com.github.phantazmnetwork.zombies.map.handler;

import com.github.phantazmnetwork.zombies.coin.TransactionModifierSource;
import com.github.phantazmnetwork.zombies.map.Window;
import org.jetbrains.annotations.NotNull;

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
