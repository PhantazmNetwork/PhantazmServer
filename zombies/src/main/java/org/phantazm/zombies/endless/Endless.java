package org.phantazm.zombies.endless;

import com.github.steanky.element.core.dependency.DependencyProvider;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.map.Round;

public interface Endless {
    @NotNull Round generateRound(int roundIndex);

    void init();

    void scaleMob(@NotNull Mob mob, int round);

    interface Source {
        @NotNull Endless make(@NotNull DependencyProvider dependencyProvider);
    }
}
