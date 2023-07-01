package org.phantazm.zombies.leaderboard;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.dependency.DependencyModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.hologram.Hologram;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.time.TickFormatter;
import org.phantazm.stats.zombies.BestTime;
import org.phantazm.stats.zombies.ZombiesDatabase;
import org.phantazm.zombies.map.MapSettingsInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

@Model("zombies.leaderboard.best_time")
public class BestTimeLeaderboard {

    private static final Logger LOGGER = LoggerFactory.getLogger(BestTimeLeaderboard.class);

    private final Data data;

    private final ZombiesDatabase database;

    private final Hologram hologram;

    private final MapSettingsInfo settings;

    private final PlayerViewProvider viewProvider;

    private final MiniMessage miniMessage;

    private final TickFormatter tickFormatter;

    private final Object sync = new Object();

    private volatile boolean active = false;

    @FactoryMethod
    public BestTimeLeaderboard(@NotNull Data data, @NotNull ZombiesDatabase database, @NotNull Hologram hologram,
            @NotNull MapSettingsInfo settings, @NotNull PlayerViewProvider viewProvider,
            @NotNull MiniMessage miniMessage, @NotNull @Child("tick_formatter") TickFormatter tickFormatter) {
        this.data = Objects.requireNonNull(data, "data");
        this.database = Objects.requireNonNull(database, "database");
        this.hologram = Objects.requireNonNull(hologram, "hologram");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.viewProvider = Objects.requireNonNull(viewProvider, "viewProvider");
        this.miniMessage = Objects.requireNonNull(miniMessage, "miniMessage");
        this.tickFormatter = Objects.requireNonNull(tickFormatter, "tickFormatter");
    }


    public void startIfNotActive() {
        synchronized (sync) {
            if (active) {
                return;
            }

            TagResolver mapNamePlaceholder = Placeholder.component("map_name", settings.displayName());
            hologram.add(miniMessage.deserialize(data.titleFormat(), mapNamePlaceholder));
            for (int i = 0; i < data.length(); ++i) {
                TagResolver placePlaceholder = Placeholder.component("place", Component.text(i + 1));
                hologram.add(miniMessage.deserialize(data.initialFormat(), placePlaceholder));
            }

            database.getBestTimes(settings.id()).whenComplete(((bestTimes, throwable) -> {
                if (throwable != null) {
                    LOGGER.warn("Failed to fetch best times", throwable);
                    return;
                }

                updateBestTimes(bestTimes);
            }));
            active = true;
        }
    }

    private void updateBestTimes(@NotNull List<BestTime> bestTimes) {
        synchronized (sync) {
            if (!active) {
                return;
            }

            while (hologram.size() > bestTimes.size() + 1) {
                hologram.remove(hologram.size() - 1);
            }

            for (int i = 1; i < hologram.size(); ++i) {
                BestTime time = bestTimes.get(i - 1);
                TagResolver placePlaceholder = Placeholder.component("place", Component.text(i));
                TagResolver timePlaceholder = Placeholder.unparsed("time", tickFormatter.format(time.time()));
                hologram.set(i, miniMessage.deserialize(data.timeFormat(), placePlaceholder, timePlaceholder));

                int finalI = i;
                viewProvider.fromUUID(time.uuid()).getDisplayName().whenComplete((displayName, throwable) -> {
                    if (throwable != null) {
                        LOGGER.warn("Failed to fetch display name for {}", time.uuid());
                        return;
                    }

                    synchronized (sync) {
                        if (!active) {
                            return;
                        }

                        TagResolver playerNamePlaceholder = Placeholder.component("player_name", displayName);
                        hologram.set(finalI,
                                miniMessage.deserialize(data.nameTimeFormat(), placePlaceholder, playerNamePlaceholder,
                                        timePlaceholder));
                    }
                });
            }
        }
    }

    public void endIfActive() {
        synchronized (sync) {
            if (active) {
                hologram.clear();
                active = false;
            }
        }
    }

    @Depend
    public static class Module implements DependencyModule {

        private final ZombiesDatabase database;

        private final Hologram hologram;

        private final MapSettingsInfo settings;

        private final PlayerViewProvider viewProvider;

        private final MiniMessage miniMessage;

        public Module(@NotNull ZombiesDatabase database, @NotNull Hologram hologram, @NotNull MapSettingsInfo settings,
                @NotNull PlayerViewProvider viewProvider, @NotNull MiniMessage miniMessage) {
            this.database = Objects.requireNonNull(database, "database");
            this.hologram = Objects.requireNonNull(hologram, "hologram");
            this.settings = Objects.requireNonNull(settings, "settings");
            this.viewProvider = Objects.requireNonNull(viewProvider, "viewProvider");
            this.miniMessage = Objects.requireNonNull(miniMessage, "miniMessage");
        }

        public @NotNull ZombiesDatabase getDatabase() {
            return database;
        }

        public @NotNull Hologram getHologram() {
            return hologram;
        }

        public @NotNull MapSettingsInfo getSettings() {
            return settings;
        }

        public @NotNull PlayerViewProvider getViewProvider() {
            return viewProvider;
        }

        public @NotNull MiniMessage getMiniMessage() {
            return miniMessage;
        }
    }

    @DataObject
    public record Data(@NotNull @ChildPath("tick_formatter") String tickFormatter,
                       @NotNull String titleFormat,
                       @NotNull String initialFormat,
                       @NotNull String timeFormat,
                       @NotNull String nameTimeFormat,
                       int length) {

    }

}
