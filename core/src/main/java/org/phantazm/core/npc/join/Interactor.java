package org.phantazm.core.npc.join;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface Interactor {
    void join(@NotNull Player player);
}
