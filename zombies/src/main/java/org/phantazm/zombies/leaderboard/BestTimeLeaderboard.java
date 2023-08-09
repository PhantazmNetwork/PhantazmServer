package org.phantazm.zombies.leaderboard;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.dependency.DependencyModule;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.hologram.Hologram;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.time.TickFormatter;
import org.phantazm.stats.zombies.BestTime;
import org.phantazm.stats.zombies.ZombiesDatabase;
import org.phantazm.zombies.map.MapSettingsInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executor;

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

    private final Executor executor;

    private final TickFormatter tickFormatter;

    private final Object sync = new Object();

    private final Int2ObjectMap<List<Component>> pages = new Int2ObjectOpenHashMap<>();

    private boolean active = false;

    private long stamp = Long.MIN_VALUE;

    private int tabIndex = -1;

    @FactoryMethod
    public BestTimeLeaderboard(@NotNull Data data, @NotNull ZombiesDatabase database, @NotNull UUID viewer,
            @NotNull Hologram hologram, @NotNull MapSettingsInfo settings, @NotNull PlayerViewProvider viewProvider,
            @NotNull MiniMessage miniMessage, @NotNull Executor executor,
            @NotNull @Child("tick_formatter") TickFormatter tickFormatter) {
        this.data = Objects.requireNonNull(data, "data");
        this.database = Objects.requireNonNull(database, "database");
        this.viewer = Objects.requireNonNull(viewer, "viewer");
        this.hologram = Objects.requireNonNull(hologram, "hologram");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.viewProvider = Objects.requireNonNull(viewProvider, "viewProvider");
        this.miniMessage = Objects.requireNonNull(miniMessage, "miniMessage");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.tickFormatter = Objects.requireNonNull(tickFormatter, "tickFormatter");
    }

    public void startIfNotActive() {
        synchronized (sync) {
            if (active) {
                return;
            }

            for (int playerCount = 1; playerCount <= 4; ++playerCount) {
                List<Component> page =
                        new ArrayList<>(data.headerFormats().size() + data.length() + data.footerFormats().size());
                TagResolver mapNamePlaceholder = Placeholder.component("map_name", settings.displayName());
                TagResolver viewerPlaceholder = Placeholder.component("viewer", data.initialMessage());
                TagResolver playerCountPlaceholder = Formatter.choice("player_count", playerCount);

                for (String headerFormat : data.headerFormats()) {
                    page.add(miniMessage.deserialize(headerFormat, mapNamePlaceholder, viewerPlaceholder,
                            playerCountPlaceholder));
                }
                for (int i = 0; i < data.length(); ++i) {
                    TagResolver placePlaceholder = Placeholder.component("place", Component.text(i + 1));
                    Component placeMessage = miniMessage.deserialize(data.placeFormat(), placePlaceholder);
                    page.add(placeMessage);
                }
                for (String footerFormat : data.footerFormats()) {
                    page.add(miniMessage.deserialize(footerFormat, mapNamePlaceholder, viewerPlaceholder,
                            playerCountPlaceholder));
                }

                pages.put(playerCountToTabIndex(playerCount), page);
            }

            renderTab(playerCountToTabIndex(4));
            tabIndex = playerCountToTabIndex(4);

            ++stamp;
            loadMapTimes(stamp);
            loadViewerTimes(stamp);
            active = true;
        }
    }

    public void cycle() {
        executor.execute(() -> {
            synchronized (sync) {
                if (!active) {
                    return;
                }

                if (tabIndex == playerCountToTabIndex(1)) {
                    tabIndex = playerCountToTabIndex(4);
                } else {
                    ++tabIndex;
                }
                renderTab(tabIndex);
            }
        });
    }

    private void renderTab(int tabIndex) {
        List<Component> page = pages.get(tabIndex);
        if (hologram.size() > page.size()) {
            hologram.subList(page.size(), hologram.size()).clear();
        }

        for (int i = hologram.size(); i < page.size(); ++i) {
            hologram.add(page.get(i));
        }
        for (int i = 0; i < hologram.size(); ++i) {
            hologram.set(i, page.get(i));
        }
    }

    public void endIfActive() {
        synchronized (sync) {
            if (!active) {
                return;
            }

            hologram.clear();
            pages.clear();
            tabIndex = -1;
            active = false;
        }
    }

    private int playerCountToTabIndex(int playerCount) {
        return 4 - playerCount;
    }

    private void loadMapTimes(long oldStamp) {
        database.getMapBestTimes(settings.id(), 1, 4, null, data.length()).whenComplete((bestTimes, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Failed to fetch best times on {}", settings.id(), throwable);
                return;
            }

            synchronized (sync) {
                if (!active || stamp != oldStamp) {
                    return;
                }

                for (int playerCount = 1; playerCount <= 4; ++playerCount) {
                    List<BestTime> bestTimeList = bestTimes.get(playerCount);
                    List<Component> page = pages.get(playerCountToTabIndex(playerCount));

                    int pageIndex = playerCountToTabIndex(playerCount);
                    if (data.headerFormats().size() + bestTimeList.size() < page.size() - data.footerFormats().size()) {
                        if (tabIndex == pageIndex) {
                            hologram.subList(data.headerFormats().size() + bestTimeList.size(),
                                    page.size() - data.footerFormats().size()).clear();
                        }

                        page.subList(data.headerFormats().size() + bestTimeList.size(),
                                page.size() - data.footerFormats().size()).clear();
                    }

                    for (int i = 0; i < bestTimeList.size(); ++i) {
                        BestTime bestTime = bestTimeList.get(i);
                        PlayerView view = viewProvider.fromUUID(bestTime.uuid());
                        Optional<? extends Component> displayNameOptional = view.getDisplayNameIfCached();
                        Component line;
                        if (displayNameOptional.isPresent()) {
                            line = makePlaceNameTimeMessage(displayNameOptional.get(), bestTime);
                        }
                        else {
                            line = makePlaceTimeMessage(bestTime);
                            loadBodyName(oldStamp, view, playerCount, bestTime);
                        }

                        page.set(data.headerFormats().size() + i, line);
                    }

                    if (tabIndex == playerCountToTabIndex(playerCount)) {
                        for (int i = data.headerFormats().size(); i < data.headerFormats().size() + bestTimeList.size();
                                ++i) {
                            hologram.set(i, page.get(i));
                        }
                    }
                }
            }
        });
    }

    private void loadBodyName(long oldStamp, PlayerView view, int playerCount, BestTime bestTime) {
        view.getDisplayName().whenComplete((displayName, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Failed to fetch display name for {}", bestTime.uuid());
                return;
            }

            synchronized (sync) {
                if (!active || stamp != oldStamp) {
                    return;
                }

                Component line = makePlaceNameTimeMessage(displayName, bestTime);
                int pageIndex = playerCountToTabIndex(playerCount);
                int lineIndex = data.headerFormats().size() + bestTime.rank() - 1;
                pages.get(pageIndex).set(lineIndex, line);
                if (tabIndex == pageIndex) {
                    hologram.set(lineIndex, line);
                }
            }
        });
    }

    private void loadViewerTimes(long oldStamp) {
        database.getMapPlayerBestTimes(viewer, settings.id(), 1, 4, null).whenComplete((bestTimes, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Failed to fetch best times for {} on {}", viewer, settings.id(), throwable);
                return;
            }

            synchronized (sync) {
                if (!active || oldStamp != stamp) {
                    return;
                }

                PlayerView view = viewProvider.fromUUID(viewer);
                view.getDisplayNameIfCached().ifPresentOrElse(displayName -> {
                    setViewerName(bestTimes, displayName);
                }, () -> loadViewerName(oldStamp, bestTimes, view));
            }
        });
    }

    private void setViewerName(Int2ObjectMap<BestTime> viewerBestTimes, Component viewerName) {
        TagResolver mapNamePlaceholder = Placeholder.component("map_name", settings.displayName());
        for (int playerCount = 1; playerCount <= 4; ++playerCount) {
            List<Component> page = pages.get(playerCountToTabIndex(playerCount));

            TagResolver playerCountPlaceholder = Formatter.choice("player_count", playerCount);
            Component viewerLine;
            if (!viewerBestTimes.containsKey(playerCount)) {
                TagResolver playerNamePlaceholder = Placeholder.component("player_name", viewerName);
                viewerLine = miniMessage.deserialize(data.noneNameFormat(), playerNamePlaceholder);
            }
            else {
                viewerLine = makePlaceNameTimeMessage(viewerName, viewerBestTimes.get(playerCount));
            }
            TagResolver viewerPlaceholder = Placeholder.component("viewer", viewerLine);

            for (int i = 0; i < data.headerFormats().size(); ++i) {
                String headerFormat = data.headerFormats().get(i);
                Component header = miniMessage.deserialize(headerFormat, mapNamePlaceholder, viewerPlaceholder,
                        playerCountPlaceholder);
                page.set(i, header);
            }

            for (int i = 0; i < data.footerFormats().size(); ++i) {
                String footerFormat = data.footerFormats().get(data.footerFormats().size() - 1 - i);
                Component footer = miniMessage.deserialize(footerFormat, mapNamePlaceholder, viewerPlaceholder);
                page.set(page.size() - 1 - i, footer);
            }

            if (tabIndex == playerCountToTabIndex(playerCount)) {
                for (int i = 0; i < data.headerFormats().size(); ++i) {
                    hologram.set(i, page.get(i));
                }

                for (int i = 0; i < data.footerFormats().size(); ++i) {
                    hologram.set(hologram.size() - 1 - i, page.get(page.size() - 1 - i));
                }
            }
        }
    }

    private void loadViewerName(long oldStamp, Int2ObjectMap<BestTime> bestTimes, PlayerView view) {
        view.getDisplayName().whenComplete((displayName, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Failed to get display name for {}", view.getUUID(), throwable);
                return;
            }

            synchronized (sync) {
                if (active && stamp == oldStamp) {
                    setViewerName(bestTimes, displayName);
                }
            }
        });
    }

    private Component makePlaceTimeMessage(BestTime bestTime) {
        TagResolver placePlaceholder = Placeholder.component("place", Component.text(bestTime.rank()));
        TagResolver timePlaceholder =
                Placeholder.component("time", Component.text(tickFormatter.format(bestTime.time())));
        TagResolver isViewerPlaceholder = Formatter.choice("is_viewer", bestTime.uuid().equals(viewer) ? 1 : 0);

        return miniMessage.deserialize(data.placeTimeFormat(), placePlaceholder, timePlaceholder, isViewerPlaceholder);
    }

    private Component makePlaceNameTimeMessage(Component playerName, BestTime bestTime) {
        TagResolver placePlaceholder = Placeholder.component("place", Component.text(bestTime.rank()));
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

        private final Executor executor;

        public Module(@NotNull ZombiesDatabase database, @NotNull UUID viewer, @NotNull Hologram hologram,
                @NotNull MapSettingsInfo settings, @NotNull PlayerViewProvider viewProvider,
                @NotNull MiniMessage miniMessage, @NotNull Executor executor) {
            this.database = Objects.requireNonNull(database, "database");
            this.viewer = Objects.requireNonNull(viewer, "viewer");
            this.hologram = Objects.requireNonNull(hologram, "hologram");
            this.settings = Objects.requireNonNull(settings, "settings");
            this.viewProvider = Objects.requireNonNull(viewProvider, "viewProvider");
            this.miniMessage = Objects.requireNonNull(miniMessage, "miniMessage");
            this.executor = Objects.requireNonNull(executor, "executor");
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

        public @NotNull Executor getExecutor() {
            return executor;
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
