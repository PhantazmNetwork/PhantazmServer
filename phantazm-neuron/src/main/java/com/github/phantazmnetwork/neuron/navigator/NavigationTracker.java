package com.github.phantazmnetwork.neuron.navigator;

import com.github.phantazmnetwork.neuron.operation.PathResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface NavigationTracker {
    NavigationTracker NULL = new NavigationTracker() {
        @Override
        public void onPathfind(@NotNull Navigator navigator) {}

        @Override
        public void onPathfindComplete(@NotNull Navigator navigator, @Nullable PathResult result) {}

        @Override
        public void onDestinationReached(@NotNull Navigator navigator) {}

        @Override
        public void onNavigationError(@NotNull Navigator navigator, @NotNull ErrorType errorType) {}
    };

    enum ErrorType {
        STUCK,
        NO_START
    }

    void onPathfind(@NotNull Navigator navigator);

    void onPathfindComplete(@NotNull Navigator navigator, @Nullable PathResult result);

    void onDestinationReached(@NotNull Navigator navigator);

    void onNavigationError(@NotNull Navigator navigator, @NotNull ErrorType errorType);
}
