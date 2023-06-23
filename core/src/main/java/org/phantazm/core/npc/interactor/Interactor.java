package org.phantazm.core.npc.interactor;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface Interactor {
    void interact(@NotNull Player player);
}
