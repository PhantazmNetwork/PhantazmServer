package org.phantazm.zombies.map.handler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.Tickable;
import org.phantazm.zombies.map.Window;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.List;

public interface WindowHandler extends Tickable {
    void handleCrouchStateChange(@NotNull ZombiesPlayer zombiesPlayer, boolean crouching);

    @NotNull @Unmodifiable List<Window> windows();

    interface Source {
        @NotNull WindowHandler make(@NotNull List<Window> windows);
    }
}