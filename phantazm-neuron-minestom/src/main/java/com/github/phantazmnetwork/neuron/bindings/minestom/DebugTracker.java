package com.github.phantazmnetwork.neuron.bindings.minestom;

import com.github.phantazmnetwork.neuron.navigator.NavigationTracker;
import com.github.phantazmnetwork.neuron.navigator.Navigator;
import com.github.phantazmnetwork.neuron.operation.PathResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DebugTracker implements NavigationTracker {
    private static final Logger logger = LoggerFactory.getLogger(DebugTracker.class);
    private static final NavigationTracker instance = new DebugTracker();

    private DebugTracker() {}

    @Override
    public void onPathfind(@NotNull Navigator navigator) {
        logger.info("Pathfinding to " + navigator.getDestination());
    }

    @Override
    public void onPathfindComplete(@NotNull Navigator navigator, @Nullable PathResult result) {
        logger.info("Completed pathfinding to " + navigator.getDestination());
    }

    @Override
    public void onDestinationReached(@NotNull Navigator navigator) {
        logger.info("Destination " + navigator.getDestination() + " reached");
    }

    @Override
    public void onNavigationError(@NotNull Navigator navigator, @NotNull ErrorType errorType) {
        logger.info("Navigation error " + errorType);
    }

    public static @NotNull NavigationTracker getInstance() {
        return instance;
    }
}
