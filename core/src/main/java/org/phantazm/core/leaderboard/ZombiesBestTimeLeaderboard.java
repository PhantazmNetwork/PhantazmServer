package org.phantazm.core.leaderboard;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import com.github.steanky.vector.Vec3D;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.FutureUtils;
import org.phantazm.commons.InjectionStore;
import org.phantazm.commons.MonoComponent;
import org.phantazm.core.RayUtils;
import org.phantazm.core.TagUtils;
import org.phantazm.core.VecUtils;
import org.phantazm.core.hologram.BasicPaginatedHologram;
import org.phantazm.core.hologram.Hologram;
import org.phantazm.core.hologram.PaginatedHologram;
import org.phantazm.core.player.IdentitySource;
import org.phantazm.core.role.RoleStore;
import org.phantazm.core.time.TickFormatter;
import org.phantazm.stats.zombies.ZombiesLeaderboardDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@Model("zombies.leaderboard.best_time")
@Cache
public class ZombiesBestTimeLeaderboard implements MonoComponent<Leaderboard> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesBestTimeLeaderboard.class);
    public static final InjectionStore.Key<Args> ARGS_KEY = InjectionStore.key(Args.class);

    private final Data data;
    private final TickFormatter tickFormatter;

    public record Args(
        @NotNull Point worldOrigin,
        @NotNull Executor executor,
        @NotNull ZombiesLeaderboardDatabase database,
        @NotNull RoleStore roleStore,
        @NotNull Function<? super @NotNull Set<Key>, ? extends @NotNull String> descriptorFunction,
        @NotNull IdentitySource identitySource) {
    }

    @FactoryMethod
    public ZombiesBestTimeLeaderboard(@NotNull Data data, @NotNull @Child("tickFormatter") TickFormatter tickFormatter) {
        this.data = data;
        this.tickFormatter = tickFormatter;
    }

    @Override
    public @NotNull Leaderboard apply(@NotNull InjectionStore injectionStore) {
        Args args = injectionStore.get(ARGS_KEY);
        return new Impl(data, args.worldOrigin, args.executor, args.database, args.roleStore, args.identitySource,
            args.descriptorFunction, tickFormatter);
    }

    public record Modifier(@NotNull Set<Key> modifiers,
        @NotNull Component name) {
    }

    private record PageKey(int teamSize,
        @NotNull ZombiesBestTimeLeaderboard.Modifier modifier) {
    }

    private record TimeEntry(int[] nameLengths,
        List<Component> names,
        long timeTaken) {
    }

    private static class Impl implements Leaderboard {
        private static final long REFRESH_RATE = Duration.ofMinutes(1).toMillis();

        private final Object sync = new Object();
        private final Tag<Integer> teamSizeIndex = Tag.Integer(TagUtils.uniqueTagName()).defaultValue(0);
        private final Tag<Integer> modifierRankingIndex = Tag.Integer(TagUtils.uniqueTagName()).defaultValue(0);

        private final Data data;
        private final Point worldOrigin;
        private final Executor executor;
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

        private Impl(Data data, Point worldOrigin, Executor executor, ZombiesLeaderboardDatabase database,
            RoleStore roleStore, IdentitySource identitySource,
            Function<? super Set<Key>, ? extends String> descriptorFunction, TickFormatter tickFormatter) {
            this.data = data;
            this.worldOrigin = worldOrigin;
            this.executor = executor;
            this.database = database;
            this.roleStore = roleStore;
            this.identitySource = identitySource;
            this.tickFormatter = tickFormatter;
            this.descriptorFunction = descriptorFunction;
            this.hologram = new BasicPaginatedHologram(VecUtils.toVec(data.location).add(worldOrigin),
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
        public void show(@NotNull Instance instance) {
            if (data.teamSizes.isEmpty() || data.modifiers.isEmpty() || shown) {
                return;
            }

            synchronized (sync) {
                if (shown) {
                    return;
                }

                hologram.setInstance(instance);

                shown = true;
                initPages();

                removeArmorStand();

                Point spawnPoint = VecUtils.toVec(data.location).add(worldOrigin)
                    .add(VecUtils.toPoint(data.armorStandOffset));
                Entity armorStand = makeInteractionEntity();
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

                hologram.destroy();
                pageRenderTimes.clear();
            }
        }

        private Entity makeInteractionEntity() {
            Entity armorStand = new Entity(EntityType.ARMOR_STAND) {
                @Override
                public void interact(@NotNull Player player, @NotNull Point position) {
                    super.interact(player, position);
                    Impl.this.interact(player, position.add(getPosition()), this);
                }

                @Override
                public void attacked(@NotNull Player player) {
                    Optional<Vec> hitOptional = RayUtils.rayTrace(this.getBoundingBox(), getPosition(),
                        player.getPosition().add(0, player.getEyeHeight(), 0));
                    if (hitOptional.isEmpty()) {
                        return;
                    }

                    Impl.this.interact(player, hitOptional.get(), this);
                }
            };

            armorStand.setInvisible(true);
            armorStand.setNoGravity(true);
            return armorStand;
        }

        private void interact(@NotNull Player player, @NotNull Point relativePosition, @NotNull Entity armorStand) {
            double midpoint = armorStand.getPosition().y() + (armorStand.getBoundingBox().height() / 2);
            int sizeIndex;
            int modifierIndex;

            //modifiers are below team sizes
            boolean updateModifier = relativePosition.y() < midpoint;

            if (updateModifier) {
                sizeIndex = player.tagHandler().getTag(teamSizeIndex);

                modifierIndex = player.tagHandler().updateAndGetTag(modifierRankingIndex, oldValue ->
                    (oldValue + 1) % data.modifiers.size());
            } else {
                sizeIndex = player.tagHandler().updateAndGetTag(teamSizeIndex, oldValue ->
                    (oldValue + 1) % data.teamSizes.size());

                modifierIndex = player.tagHandler().getTag(modifierRankingIndex);
            }

            int teamSize = data.teamSizes.getInt(sizeIndex);
            Modifier modifier = data.modifiers.get(modifierIndex);

            PageKey key = new PageKey(teamSize, modifier);
            synchronized (sync) {
                renderTimesForPage(key).whenCompleteAsync((ignored, throwable) -> {
                    if (throwable != null) {
                        LOGGER.warn("Error when rendering leaderboard times", throwable);
                    }
                }, executor);
            }

            int targetPage = pageMap.getInt(key);
            if (targetPage == -1) {
                return;
            }

            hologram.setPage(player, targetPage);
            player.playSound(data.clickSound);
        }

        private Component constructTeamLineFor(int teamSize) {
            List<Component> formattedLines = new ArrayList<>(data.teamSizeToNameMappings.size());
            for (Int2ObjectMap.Entry<String> entry : data.teamSizeToNameMappings.int2ObjectEntrySet()) {
                int teamSizeKey = entry.getIntKey();
                TagResolver teamSizeNameTag = Placeholder.parsed("team_size_name", entry.getValue());

                formattedLines.add(teamSizeKey == teamSize ?
                    MiniMessage.miniMessage().deserialize(data.activeTeamSizeNameFormat, teamSizeNameTag) :
                    MiniMessage.miniMessage().deserialize(data.inactiveTeamSizeNameFormat, teamSizeNameTag));
            }

            return Component.join(JoinConfiguration.separator(Component.space()), formattedLines);
        }

        private void initPages() {
            IntIterator teamSizeIterator = data.teamSizes.intIterator();
            while (teamSizeIterator.hasNext()) {
                int teamSize = teamSizeIterator.nextInt();

                String teamSizeName = data.teamSizeToNameMappings.get(teamSize);
                if (teamSizeName == null) {
                    teamSizeName = Integer.toString(teamSize);
                }

                TagResolver teamSizeNameTag = Placeholder.parsed("team_size_name", teamSizeName);

                Component teamSizeLine = constructTeamLineFor(teamSize);

                for (Modifier modifier : data.modifiers) {
                    TagResolver modifierNameTag = Placeholder.component("modifier_name", modifier.name);

                    List<Hologram.Line> pageContents = new ArrayList<>(data.headerFormats.size() + data.footerFormats.size() + 3);
                    for (String format : data.headerFormats) {
                        pageContents.add(Hologram
                            .line(MiniMessage.miniMessage().deserialize(format, modifierNameTag, teamSizeNameTag),
                                data.gap));
                    }

                    for (String format : data.footerFormats) {
                        pageContents.add(Hologram
                            .line(MiniMessage.miniMessage().deserialize(format, modifierNameTag, teamSizeNameTag),
                                data.gap));
                    }

                    String descriptor = descriptorFunction.apply(modifier.modifiers);
                    pageContents.add(Hologram.line(data.viewingPlayerFormat, Hologram.formatter(data.loading,
                        (string, player) -> {
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
                        }), data.gap));

                    pageContents.add(Hologram.line(teamSizeLine, data.gap));

                    List<Component> formattedLines = new ArrayList<>(data.teamSizeToNameMappings.size());
                    for (Modifier mod : data.modifiers) {
                        TagResolver modifierTag = Placeholder.component("modifier_name", mod.name);

                        formattedLines.add(mod == modifier ?
                            MiniMessage.miniMessage().deserialize(data.activeModifierNameFormat, modifierTag) :
                            MiniMessage.miniMessage().deserialize(data.inactiveModifierNameFormat, modifierTag));
                    }

                    pageContents.add(Hologram.line(Component.join(JoinConfiguration.separator(Component.space()),
                        formattedLines), data.gap));

                    hologram.addPage(pageContents, Hologram.Alignment.LOWER);
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
                TimeEntry[] timeEntries = new TimeEntry[entries.size()];
                CompletableFuture<?>[] entryFutures = new CompletableFuture[entries.size()];
                for (int i = 0; i < entries.size(); i++) {
                    ZombiesLeaderboardDatabase.LeaderboardEntry entry = entries.get(i);

                    int thisTeamSize = entry.team().size();
                    int[] nameLengths = new int[thisTeamSize];
                    CompletableFuture<Component>[] names = new CompletableFuture[thisTeamSize];

                    int j = 0;
                    for (UUID member : entry.team()) {
                        int thisJ = j++;
                        names[thisJ] = identitySource.getName(member).thenApplyAsync(nameOptional -> {
                            return nameOptional.orElse("?");
                        }, executor).thenComposeAsync(name -> roleStore.getStylingRole(member).thenApplyAsync(role -> {
                            nameLengths[thisJ] = name.length();
                            return role.styleRawName(name);
                        }, executor), executor);
                    }

                    int entryIndex = i;
                    entryFutures[i] = CompletableFuture.allOf(names).thenRunAsync(() -> {
                        List<Component> finishedNames = new ArrayList<>(names.length);
                        for (CompletableFuture<Component> name : names) {
                            finishedNames.add(Objects.requireNonNull(name.getNow(null)));
                        }

                        timeEntries[entryIndex] = new TimeEntry(nameLengths, finishedNames, entry.timeTaken());
                    }, executor);
                }

                CompletableFuture.allOf(entryFutures).thenRunAsync(() -> {
                    hologram.updatePage(page, hologram -> {
                        int insertStart = data.headerFormats.size();
                        int insertEnd = hologram.size() - data.footerFormats.size() - 3;
                        if (insertEnd >= insertStart) {
                            hologram.subList(insertStart, insertEnd).clear();
                        }

                        List<List<Component>> rowsToAdd = new ArrayList<>(2);
                        List<Component> rowBuffer = new ArrayList<>(2);

                        List<Hologram.Line> linesToAdd = new ArrayList<>(timeEntries.length);

                        for (int i = 0; i < timeEntries.length; i++) {
                            TimeEntry entry = timeEntries[i];
                            TagResolver rankTag = Placeholder.unparsed("rank", Integer.toString(start + 1 + i));
                            TagResolver timeTag = Placeholder.unparsed("time", tickFormatter.format(entry.timeTaken()));

                            int lengthThisRow = 0;

                            for (int j = 0; j < entry.names.size(); j++) {
                                Component name = entry.names.get(j);
                                int thisNameLength = entry.nameLengths[j];
                                if (rowBuffer.isEmpty() || lengthThisRow + thisNameLength < data.widthBeforeLineBreak) {
                                    rowBuffer.add(name);
                                } else {
                                    lengthThisRow = 0;
                                    rowsToAdd.add(List.copyOf(rowBuffer));

                                    rowBuffer.clear();
                                    rowBuffer.add(name);
                                }

                                lengthThisRow += thisNameLength;
                            }

                            //there is always at least one name in the buffer
                            rowsToAdd.add(rowBuffer);

                            for (int j = 0; j < rowsToAdd.size(); j++) {
                                List<Component> row = rowsToAdd.get(j);

                                TagResolver namesTag = Placeholder.component("names",
                                    Component.join(JoinConfiguration.commas(true), row));

                                double gap = j < rowsToAdd.size() - 1 ? data.lineBreakGap : data.leaderboardEntryGap;

                                String formatString = selectFormat(rowsToAdd.size(), j);

                                Component formatted = MiniMessage.miniMessage().deserialize(formatString, rankTag,
                                    timeTag, namesTag);

                                linesToAdd.add(Hologram.line(formatted, gap));
                            }

                            rowsToAdd.clear();
                            rowBuffer.clear();
                        }

                        hologram.addAll(insertStart, linesToAdd);
                    });
                }, executor);
            }, executor);
        }

        private String selectFormat(int size, int j) {
            if (size == 1) {
                return data.singleLineFormat;
            } else if (j == 0) {
                return data.firstLineFormat;
            } else if (j == size - 1) {
                return data.lastLineFormat;
            } else {
                return data.middleLineFormat;
            }
        }
    }

    @Default("""
        {
          armorStandOffset={x=0, y=-0.20, z=0},
          widthBeforeLineBreak=32,
          lineBreakGap=0.0,
          leaderboardEntryGap=0.1,
          gap=0.05
        }
        """)
    @DataObject
    public record Data(@NotNull Vec3D location,
        @NotNull Vec3D armorStandOffset,
        @NotNull Key map,
        @NotNull IntList teamSizes,
        @NotNull List<Modifier> modifiers,
        @NotNull Int2ObjectSortedMap<String> teamSizeToNameMappings,
        @NotNull Int2IntMap teamSizeToEntryCountMappings,
        @NotNull List<String> headerFormats,
        @NotNull List<String> footerFormats,
        @NotNull String viewingPlayerFormat,
        @NotNull Component loading,
        @NotNull String singleLineFormat,
        @NotNull String firstLineFormat,
        @NotNull String middleLineFormat,
        @NotNull String lastLineFormat,
        @NotNull Component unknownRank,
        @NotNull Component unknownTime,
        @NotNull Sound clickSound,
        @NotNull String inactiveTeamSizeNameFormat,
        @NotNull String activeTeamSizeNameFormat,
        @NotNull String inactiveModifierNameFormat,
        @NotNull String activeModifierNameFormat,
        int widthBeforeLineBreak,
        double lineBreakGap,
        double leaderboardEntryGap,
        double gap) {
    }
}
