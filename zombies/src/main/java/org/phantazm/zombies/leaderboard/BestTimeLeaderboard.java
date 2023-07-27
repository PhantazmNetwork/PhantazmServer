package org.phantazm.zombies.leaderboard;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.dependency.DependencyModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
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

import java.util.*;

@Model("zombies.leaderboard.best_time")
@Cache(false)
public class BestTimeLeaderboard {

    private static final Logger LOGGER = LoggerFactory.getLogger(BestTimeLeaderboard.class);

    private final Data data;

    private final ZombiesDatabase database;

    private final UUID viewer;

    private final Hologram hologram;

    private final MapSettingsInfo settings;

    private final PlayerViewProvider viewProvider;

    private final MiniMessage miniMessage;

    private final TickFormatter tickFormatter;

    private final Object sync = new Object();

    private volatile boolean active = false;

    @FactoryMethod
    public BestTimeLeaderboard(@NotNull Data data, @NotNull ZombiesDatabase database, @NotNull UUID viewer,
            @NotNull Hologram hologram, @NotNull MapSettingsInfo settings, @NotNull PlayerViewProvider viewProvider,
            @NotNull MiniMessage miniMessage, @NotNull @Child("tick_formatter") TickFormatter tickFormatter) {
        this.data = Objects.requireNonNull(data, "data");
        this.database = Objects.requireNonNull(database, "database");
        this.viewer = Objects.requireNonNull(viewer, "viewer");
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
            TagResolver viewerPlaceholder = Placeholder.component("viewer", data.initialMessage());
            for (String headerFormat : data.headerFormats()) {
                hologram.add(miniMessage.deserialize(headerFormat, mapNamePlaceholder, viewerPlaceholder));
            }
            for (int i = 0; i < data.length(); ++i) {
                TagResolver placePlaceholder = Placeholder.component("place", Component.text(i + 1));
                Component placeMessage = miniMessage.deserialize(data.placeFormat(), placePlaceholder);
                hologram.add(placeMessage);
            }
            for (String footerFormat : data.footerFormats()) {
                hologram.add(miniMessage.deserialize(footerFormat, mapNamePlaceholder, viewerPlaceholder));
            }

            updateBody();
            updateViewerTime(mapNamePlaceholder);
            active = true;
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

    private void updateBody() {
        database.getBestTimes(settings.id(), data.length()).whenComplete((bestTimes, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Failed to fetch best times for {}", settings.id());
                return;
            }

            synchronized (sync) {
                if (!active) {
                    return;
                }

                trimBody(bestTimes);

                for (int i = 0; i < bestTimes.size(); ++i) {
                    BestTime bestTime = bestTimes.get(i);
                    int index = data.headerFormats().size() + i;
                    hologram.set(index, makePlaceTimeMessage(i + 1, bestTime));
                    updateBodyName(index, i + 1, bestTime);
                }
            }
        });
    }

    private void trimBody(List<BestTime> bestTimes) {
        for (int i = data.length - 1; i >= bestTimes.size(); --i) {
            hologram.remove(data.headerFormats().size() + i);
        }
    }

    private void updateBodyName(int index, int place, BestTime bestTime) {
        viewProvider.fromUUID(bestTime.uuid()).getDisplayName().whenComplete((displayName, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Failed to get display name for {}", bestTime.uuid(), throwable);
                return;
            }

            synchronized (sync) {
                if (!active) {
                    return;
                }
            }

            hologram.set(index, makePlaceNameTimeMessage(place, displayName, bestTime));
        });
    }

    private void updateViewerTime(TagResolver mapNamePlaceholder) {
        database.getBestTime(viewer, settings.id()).whenComplete((bestTimeOptional, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Failed to fetch best time for {} on {}", viewer, settings.id(), throwable);
                return;
            }

            synchronized (sync) {
                if (!active) {
                    return;
                }

                Component updatedViewerComponent;
                if (bestTimeOptional.isPresent()) {
                    BestTime bestTime = bestTimeOptional.get();
                    updatedViewerComponent = makePlaceTimeMessage(bestTime.rank(), bestTime);
                }
                else {
                    updatedViewerComponent = miniMessage.deserialize(data.noneFormat());
                }

                updateHeaderFooter(mapNamePlaceholder, Placeholder.component("viewer", updatedViewerComponent));
                updateViewerName(mapNamePlaceholder, bestTimeOptional.orElse(null));
            }
        });
    }

    private void updateViewerName(TagResolver mapNamePlaceholder, BestTime bestTime) {
        viewProvider.fromUUID(viewer).getDisplayName().whenComplete((displayName, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Failed to fetch name for {}", viewer, throwable);
                return;
            }

            synchronized (sync) {
                if (!active) {
                    return;
                }

                Component updatedViewerComponent;
                if (bestTime != null) {
                    updatedViewerComponent = makePlaceNameTimeMessage(bestTime.rank(), displayName, bestTime);
                }
                else {
                    updatedViewerComponent = miniMessage.deserialize(data.noneNameFormat(),
                            Placeholder.component("player_name", displayName));
                }

                updateHeaderFooter(mapNamePlaceholder, Placeholder.component("viewer", updatedViewerComponent));
            }
        });
    }

    private void updateHeaderFooter(TagResolver mapNamePlaceholder, TagResolver viewerPlaceholder) {
        for (int i = 0; i < data.headerFormats().size(); ++i) {
            String headerFormat = data.headerFormats().get(i);
            hologram.set(i, miniMessage.deserialize(headerFormat, mapNamePlaceholder, viewerPlaceholder));
        }

        for (int i = 0; i < data.footerFormats().size(); ++i) {
            String footerFormat = data.footerFormats().get(data.footerFormats().size() - 1 - i);
            hologram.set(hologram.size() - 1 - i,
                    miniMessage.deserialize(footerFormat, mapNamePlaceholder, viewerPlaceholder));
        }
    }

    private Component makePlaceTimeMessage(int place, BestTime bestTime) {
        TagResolver placePlaceholder = Placeholder.component("place", Component.text(place));
        TagResolver timePlaceholder =
                Placeholder.component("time", Component.text(tickFormatter.format(bestTime.time())));
        TagResolver isViewerPlaceholder = Formatter.choice("is_viewer", bestTime.uuid().equals(viewer) ? 1 : 0);

        return miniMessage.deserialize(data.placeTimeFormat(), placePlaceholder, timePlaceholder, isViewerPlaceholder);
    }

    private Component makePlaceNameTimeMessage(int place, Component playerName, BestTime bestTime) {
        TagResolver placePlaceholder = Placeholder.component("place", Component.text(place));
        TagResolver playerNamePlaceholder = Placeholder.component("player_name", playerName);
        TagResolver timePlaceholder =
                Placeholder.component("time", Component.text(tickFormatter.format(bestTime.time())));
        TagResolver isViewerPlaceholder = Formatter.choice("is_viewer", bestTime.uuid().equals(viewer) ? 1 : 0);

        return miniMessage.deserialize(data.placeNameTimeFormat(), placePlaceholder, playerNamePlaceholder,
                timePlaceholder, isViewerPlaceholder);
    }

    @Depend
    public static class Module implements DependencyModule {

        private final ZombiesDatabase database;

        private final Hologram hologram;

        private final UUID viewer;

        private final MapSettingsInfo settings;

        private final PlayerViewProvider viewProvider;

        private final MiniMessage miniMessage;

        public Module(@NotNull ZombiesDatabase database, @NotNull UUID viewer, @NotNull Hologram hologram,
                @NotNull MapSettingsInfo settings, @NotNull PlayerViewProvider viewProvider,
                @NotNull MiniMessage miniMessage) {
            this.database = Objects.requireNonNull(database, "database");
            this.viewer = Objects.requireNonNull(viewer, "viewer");
            this.hologram = Objects.requireNonNull(hologram, "hologram");
            this.settings = Objects.requireNonNull(settings, "settings");
            this.viewProvider = Objects.requireNonNull(viewProvider, "viewProvider");
            this.miniMessage = Objects.requireNonNull(miniMessage, "miniMessage");
        }

        public @NotNull ZombiesDatabase getDatabase() {
            return database;
        }

        public @NotNull UUID getViewer() {
            return viewer;
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
                       @NotNull List<String> headerFormats,
                       @NotNull Component initialMessage,
                       @NotNull String noneFormat,
                       @NotNull String noneNameFormat,
                       @NotNull String placeFormat,
                       @NotNull String placeTimeFormat,
                       @NotNull String placeNameTimeFormat,
                       @NotNull List<String> footerFormats,
                       int length) {

    }

}
