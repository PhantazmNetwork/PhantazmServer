package org.phantazm.zombies.endless;

import com.github.steanky.element.core.dependency.DependencyProvider;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Round;

public interface Endless {
    @NotNull Round generateRound(int roundIndex);

    void init();

    interface Source {
        @NotNull Endless make(@NotNull DependencyProvider dependencyProvider);
    }
}
