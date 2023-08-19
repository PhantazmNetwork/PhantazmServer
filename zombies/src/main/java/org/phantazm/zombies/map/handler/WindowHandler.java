package org.phantazm.zombies.map.handler;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Tickable;
import org.phantazm.core.tracker.BoundedTracker;
import org.phantazm.mob.MobStore;
import org.phantazm.zombies.map.Room;
import org.phantazm.zombies.map.Window;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Collection;

public interface WindowHandler extends Tickable {
    void handleCrouchStateChange(@NotNull ZombiesPlayer zombiesPlayer, boolean crouching);

    @NotNull BoundedTracker<Window> tracker();

    interface Source {
        @NotNull WindowHandler make(@NotNull BoundedTracker<Window> windowTracker,
            @NotNull BoundedTracker<Room> roomTracker, @NotNull MobStore mobStore,
            @NotNull Collection<? extends ZombiesPlayer> players);
    }

    record WindowMessages(
        @NotNull Component nearWindow,
        @NotNull Component startRepairing,
        @NotNull Component stopRepairing,
        @NotNull Component finishRepairing,
        @NotNull Component enemiesNearby) {

    }
}
