package org.phantazm.zombies.leaderboard;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.hologram.Hologram;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.time.TickFormatter;
import org.phantazm.stats.zombies.BestTime;
import org.phantazm.stats.zombies.ZombiesDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class BestTimeLeaderboard {

    private static final Logger LOGGER = LoggerFactory.getLogger(BestTimeLeaderboard.class);

    private final ZombiesDatabase database;

    private final Hologram hologram;

    private final Component mapName;

    private final Key mapKey;

    private final TickFormatter tickFormatter;

    private final PlayerViewProvider viewProvider;

    private final int maxLength;

    private final Collection<Future<?>> nameFutures = new ArrayList<>();

    private Future<?> bestTimesFuture = null;

    public BestTimeLeaderboard(@NotNull ZombiesDatabase database, @NotNull Hologram hologram,
            @NotNull Component mapName, @NotNull Key mapKey, @NotNull TickFormatter tickFormatter,
            @NotNull PlayerViewProvider viewProvider, int maxLength) {
        this.database = Objects.requireNonNull(database, "database");
        this.hologram = Objects.requireNonNull(hologram, "hologram");
        this.mapName = Objects.requireNonNull(mapName, "mapName");
        this.mapKey = Objects.requireNonNull(mapKey, "mapKey");
        this.tickFormatter = Objects.requireNonNull(tickFormatter, "tickFormatter");
        this.viewProvider = Objects.requireNonNull(viewProvider, "viewProvider");
        this.maxLength = maxLength;
    }

    public void start() {
        hologram.add(Component.textOfChildren(Component.text("Zombies - "), mapName));
        for (int i = 0; i < maxLength; ++i) {
            hologram.add(Component.text((i + 1) + ". - Loading..."));
        }

        bestTimesFuture = database.getBestTimes(mapKey).whenComplete(((bestTimes, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Failed to fetch best times", throwable);
                return;
            }

            updateBestTimes(bestTimes);
        }));
    }

    public void updateBestTimes(@NotNull List<BestTime> bestTimes) {
        while (hologram.size() != bestTimes.size() + 1) {
            hologram.remove(hologram.size() - 1);
        }

        for (int i = 0; i < bestTimes.size(); ++i) {
            BestTime time = bestTimes.get(i);
            Component timeComponent = tickFormatter.format(time.time());
            hologram.set(i, Component.textOfChildren(Component.text((i + 1) + ". - Loading... - "), timeComponent));

            int finalI = i;
            CompletableFuture<?> future =
                    viewProvider.fromUUID(time.uuid()).getDisplayName().whenComplete((displayName, throwable) -> {
                        if (throwable != null) {
                            LOGGER.warn("Failed to fetch display name for {}", time.uuid());
                            return;
                        }

                        hologram.set(finalI,
                                Component.textOfChildren(Component.text((finalI + 1) + ". - "), displayName,
                                        Component.text(" - "), timeComponent));
                    });
            nameFutures.add(future);
        }
    }

    public void end() {
        if (bestTimesFuture != null) {
            bestTimesFuture.cancel(true);
        }

        for (Future<?> future : nameFutures) {
            future.cancel(true);
        }
        nameFutures.clear();
    }

}
