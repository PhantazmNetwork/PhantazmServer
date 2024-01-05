package org.phantazm.core.leaderboard;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.vector.Vec3D;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.Taggable;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.DualComponent;
import org.phantazm.commons.FutureUtils;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.TagUtils;
import org.phantazm.core.VecUtils;
import org.phantazm.core.hologram.BasicPaginatedHologram;
import org.phantazm.core.hologram.Hologram;
import org.phantazm.core.hologram.PaginatedHologram;
import org.phantazm.core.hologram.ViewableHologram;
import org.phantazm.core.player.IdentitySource;
import org.phantazm.core.role.RoleStore;
import org.phantazm.core.time.TickFormatter;
import org.phantazm.stats.zombies.ZombiesLeaderboardDatabase;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@Model("zombies.leaderboard.best_time_2")
@Cache(false)
public class ZombiesBestTimeLeaderboard implements DualComponent<ZombiesBestTimeLeaderboard.Args, ZombiesBestTimeLeaderboard.Impl> {
    private final Data data;
    private final TickFormatter tickFormatter;

    public record Args(
        @NotNull Executor executor,
        @NotNull Instance instance,
        @NotNull ZombiesLeaderboardDatabase database,
        @NotNull RoleStore roleStore,
        @NotNull Function<? super @NotNull Set<Key>, ? extends @NotNull String> descriptorFunction,
        @NotNull IdentitySource identitySource) {
    }

    @FactoryMethod
    public ZombiesBestTimeLeaderboard(@NotNull Data data, @NotNull @Child("tick_formatter") TickFormatter tickFormatter) {
        this.data = data;
        this.tickFormatter = tickFormatter;
    }

    @Override
    public @NotNull ZombiesBestTimeLeaderboard.Impl apply(@NotNull InjectionStore injectionStore, @NotNull Args args) {
        return new Impl(data, args.executor, args.instance, args.database, args.roleStore, args.identitySource,
            args.descriptorFunction, tickFormatter);
    }

    public record Modifier(@NotNull Set<Key> modifiers,
        @NotNull Component name) {
    }

    public static class Impl implements Leaderboard {
        private static final long REFRESH_RATE = Duration.ofMinutes(1).toMillis();

        private final Object sync = new Object();
        private final Tag<Integer> teamSizeIndex = Tag.Integer(TagUtils.uniqueTagName()).defaultValue(0);
        private final Tag<Integer> modifierRankingIndex = Tag.Integer(TagUtils.uniqueTagName()).defaultValue(0);

        private final Data data;
        private final Executor executor;
        private final Instance instance;
        private final ZombiesLeaderboardDatabase database;
        private final RoleStore roleStore;
        private final IdentitySource identitySource;
        private final Function<? super Set<Key>, ? extends String> descriptorFunction;
        private final TickFormatter tickFormatter;

        private final PaginatedHologram hologram;
        private final Object2IntMap<PageKey> pageMap;

        private final Int2LongMap pageRenderTimes;

        private Entity interactionPoint;
        private boolean shown;

        private Impl(Data data, Executor executor, Instance instance, ZombiesLeaderboardDatabase database,
            RoleStore roleStore, IdentitySource identitySource, Function<? super Set<Key>, ? extends String> descriptorFunction, TickFormatter tickFormatter) {
            this.data = data;
            this.executor = executor;
            this.instance = instance;
            this.database = database;
            this.roleStore = roleStore;
            this.identitySource = identitySource;
            this.tickFormatter = tickFormatter;
            this.descriptorFunction = descriptorFunction;
            this.hologram = new BasicPaginatedHologram(VecUtils.toVec(data.location),
                Tag.Integer(TagUtils.uniqueTagName()).defaultValue(0));
            this.pageMap = new Object2IntOpenHashMap<>();
            this.pageMap.defaultReturnValue(-1);
            this.pageRenderTimes = new Int2LongOpenHashMap();

            data.teamSizeToEntryCountMappings.defaultReturnValue(12);
            initPageMap();
        }

        private void initPageMap() {
            int i = 0;
            IntIterator teamSizeIterator = data.teamSizes.intIterator();
            while (teamSizeIterator.hasNext()) {
                int teamSize = teamSizeIterator.nextInt();

                for (Modifier modifier : data.modifiers) {
                    pageMap.put(new PageKey(teamSize, modifier), i++);
                }
            }
        }

        private void removeArmorStand() {
            Entity armorStand = this.interactionPoint;
            if (armorStand != null) {
                armorStand.remove();
                this.interactionPoint = null;
            }
        }

        @Override
        public void show() {
            if (data.teamSizes.isEmpty() || data.modifiers.isEmpty() || shown) {
                return;
            }

            synchronized (sync) {
                if (shown) {
                    return;
                }

                shown = true;
                initPages();

                removeArmorStand();

                Point spawnPoint = VecUtils.toPoint(data.location.add(data.armorStandOffset));
                Entity armorStand = new Entity(EntityType.ARMOR_STAND) {
                    @Override
                    public void interact(@NotNull Player player, @NotNull Point position) {
                        super.interact(player, position);
                        ZombiesBestTimeLeaderboard.Impl.this.interact(player, position.sub(spawnPoint), this);
                    }
                };
                armorStand.setNoGravity(true);
                armorStand.setInvisible(true);
                armorStand.setInstance(instance, spawnPoint);

                this.interactionPoint = armorStand;

                //always render the first page
                renderTimesForPage(new PageKey(data.teamSizes.getInt(0), data.modifiers.get(0)));
            }
        }

        @Override
        public void hide() {
            if (data.teamSizes.isEmpty() || data.modifiers.isEmpty() || !shown) {
                return;
            }

            synchronized (sync) {
                if (!shown) {
                    return;
                }

                shown = false;
                removeArmorStand();

                hologram.clear();
                pageRenderTimes.clear();
            }
        }

        private void interact(@NotNull Taggable taggable, @NotNull Point relativePosition, @NotNull Entity armorStand) {
            double midpoint = data.location.y() + (armorStand.getBoundingBox().height() / 2);
            int sizeIndex;
            int modifierIndex;

            if (relativePosition.y() < midpoint) {
                sizeIndex = taggable.tagHandler().getTag(teamSizeIndex);

                modifierIndex = taggable.tagHandler().updateAndGetTag(modifierRankingIndex, oldValue ->
                    (oldValue + 1) % data.modifiers.size());
            } else {
                sizeIndex = taggable.tagHandler().updateAndGetTag(teamSizeIndex, oldValue ->
                    (oldValue + 1) % data.teamSizes.size());

                modifierIndex = taggable.tagHandler().getTag(modifierRankingIndex);
            }

            int teamSize = data.teamSizes.getInt(sizeIndex);
            Modifier modifier = data.modifiers.get(modifierIndex);

            PageKey key = new PageKey(teamSize, modifier);
            synchronized (sync) {
                renderTimesForPage(key);
            }

            int targetPage = pageMap.getInt(key);
            if (targetPage == -1) {
                return;
            }

            hologram.setPage(taggable, targetPage);
        }

        private record PageKey(int teamSize,
            @NotNull ZombiesBestTimeLeaderboard.Modifier modifier) {
        }

        private void initPages() {
            IntIterator teamSizeIterator = data.teamSizes.intIterator();
            while (teamSizeIterator.hasNext()) {
                int teamSize = teamSizeIterator.nextInt();

                Component teamSizeName = data.teamSizeToNameMappings.get(teamSize);
                if (teamSizeName == null) {
                    teamSizeName = Component.text(teamSize);
                }

                for (Modifier modifier : data.modifiers) {
                    TagResolver modifierNameTag = Placeholder.component("modifier_name", modifier.name);
                    TagResolver teamSizeNameTag = Placeholder.component("team_size_name", teamSizeName);

                    List<PaginatedHologram.PageLine> pageContents = new ArrayList<>(data.headerFormats.size() + data.footerFormats.size() + 1);
                    for (String format : data.headerFormats) {
                        pageContents.add(PaginatedHologram
                            .line(MiniMessage.miniMessage().deserialize(format, modifierNameTag, teamSizeNameTag)));
                    }

                    for (String format : data.footerFormats) {
                        pageContents.add(PaginatedHologram
                            .line(MiniMessage.miniMessage().deserialize(format, modifierNameTag, teamSizeNameTag)));
                    }

                    pageContents.add(PaginatedHologram.line(data.viewingPlayerFormat));

                    String descriptor = descriptorFunction.apply(modifier.modifiers);
                    hologram.addPage(pageContents, 0, Hologram.Alignment.LOWER, new ViewableHologram.LineFormatter() {
                        @Override
                        public @NotNull Component initialValue() {
                            return data.loading;
                        }

                        @Override
                        public CompletableFuture<Component> apply(String string, Player player) {
                            return database.fetchBestRanking(player.getUuid(), teamSize, data.map, descriptor)
                                .thenComposeAsync(rankingOptional ->
                                    roleStore.getStylingRole(player.getUuid()).thenApplyAsync(role -> {
                                        Component name = role.styleRawName(player.getUsername());

                                        TagResolver nameTag = Placeholder.component("name", name);

                                        TagResolver rankTag;
                                        TagResolver timeTag;
                                        if (rankingOptional.isPresent()) {
                                            ZombiesLeaderboardDatabase.RankingEntry rank = rankingOptional.get();
                                            rankTag = Placeholder.unparsed("rank", Integer.toString(rank.rank()));
                                            timeTag = Placeholder.unparsed("time", tickFormatter.format(rank.timeTaken()));
                                        } else {
                                            rankTag = Placeholder.component("rank", data.unknownRank);
                                            timeTag = Placeholder.component("time", data.unknownTime);
                                        }

                                        return MiniMessage.miniMessage().deserialize(string, nameTag, rankTag, timeTag);
                                    }, executor), executor);
                        }
                    });
                }
            }
        }

        @SuppressWarnings("unchecked")
        private CompletableFuture<Void> renderTimesForPage(PageKey key) {
            int page = pageMap.getInt(key);
            if (page == -1) {
                return FutureUtils.nullCompletedFuture();
            }

            long lastRender = pageRenderTimes.get(page);
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastRender < REFRESH_RATE) {
                return FutureUtils.nullCompletedFuture();
            }

            pageRenderTimes.put(page, currentTime);

            Modifier modifier = key.modifier;
            int teamSize = key.teamSize;

            String descriptor = descriptorFunction.apply(modifier.modifiers);
            int count = data.teamSizeToEntryCountMappings.get(teamSize);
            int start = 0;
            return database.fetchBestTimes(teamSize, descriptor, data.map, start, count).thenAcceptAsync(entries -> {
                Component[] timeEntries = new Component[entries.size()];
                CompletableFuture<?>[] entryFutures = new CompletableFuture[entries.size()];
                for (int i = 0; i < entries.size(); i++) {
                    ZombiesLeaderboardDatabase.LeaderboardEntry entry = entries.get(i);

                    TagResolver rankTag = Placeholder.unparsed("rank", Integer.toString(start + 1 + i));
                    TagResolver timeTag = Placeholder.unparsed("text", tickFormatter.format(entry.timeTaken()));

                    CompletableFuture<Component>[] names = new CompletableFuture[entry.team().size()];

                    int j = 0;
                    for (UUID member : entry.team()) {
                        names[j++] = identitySource.getName(member).thenApplyAsync(nameOptional -> {
                            return nameOptional.orElse("Unknown User");
                        }, executor).thenComposeAsync(name -> roleStore.getStylingRole(member).thenApplyAsync(role -> {
                            return role.styleRawName(name);
                        }, executor), executor);
                    }

                    int entryIndex = i;
                    entryFutures[i] = CompletableFuture.allOf(names).thenRunAsync(() -> {
                        List<Component> finishedNames = new ArrayList<>(names.length);
                        for (CompletableFuture<Component> name : names) {
                            finishedNames.add(Objects.requireNonNull(name.getNow(null)));
                        }

                        Component joinedNames = Component.join(JoinConfiguration.commas(true), finishedNames);

                        TagResolver namesTag = Placeholder.component("names", joinedNames);

                        timeEntries[entryIndex] = MiniMessage.miniMessage().deserialize(data.scoreFormat, rankTag,
                            timeTag, namesTag);
                    }, executor);
                }

                CompletableFuture.allOf(entryFutures).thenRunAsync(() -> {
                    hologram.updatePage(page, hologram -> {
                        int insertStart = data.headerFormats.size();
                        int insertEnd = hologram.size() - data.footerFormats.size();
                        if (insertEnd >= insertStart) {
                            hologram.subList(insertStart, insertEnd).clear();
                        }

                        hologram.addAll(insertStart, List.of(timeEntries));
                    });
                }, executor);
            }, executor);
        }
    }

    @DataObject
    public record Data(@NotNull Vec3D location,
        @NotNull Vec3D armorStandOffset,
        @NotNull @ChildPath("tick_formatter") String tickFormatter,
        @NotNull Key map,
        @NotNull IntList teamSizes,
        @NotNull List<Modifier> modifiers,
        @NotNull Int2ObjectMap<Component> teamSizeToNameMappings,
        @NotNull Int2IntMap teamSizeToEntryCountMappings,
        @NotNull List<String> headerFormats,
        @NotNull List<String> footerFormats,
        @NotNull String viewingPlayerFormat,
        @NotNull Component loading,
        @NotNull String scoreFormat,
        @NotNull Component unknownRank,
        @NotNull Component unknownTime) {
    }
}
