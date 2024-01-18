package org.phantazm.core.leaderboard;

import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public interface Leaderboard {
    void show(@NotNull Instance instance);

    void hide();
}
