package com.github.phantazmnetwork.zombies.map.handler;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.zombies.coin.TransactionModifierSource;
import com.github.phantazmnetwork.zombies.map.Window;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface WindowHandler extends Tickable {
    void handleCrouchStateChange(@NotNull ZombiesPlayer zombiesPlayer, @NotNull Point position, boolean crouching);

    interface Source {
        @NotNull WindowHandler make(@NotNull List<Window> windows,
                @NotNull TransactionModifierSource mapTransactionModifiers);
    }
}
