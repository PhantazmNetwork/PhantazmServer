package org.phantazm.zombies.leaderboard;

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
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.DualComponent;
import org.phantazm.commons.FutureUtils;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.VecUtils;
import org.phantazm.core.hologram.BasicPaginatedHologram;
import org.phantazm.core.hologram.Hologram;
import org.phantazm.core.hologram.PaginatedHologram;
import org.phantazm.core.leaderboard.Leaderboard;
import org.phantazm.core.player.IdentitySource;
import org.phantazm.core.role.RoleStore;
import org.phantazm.core.time.TickFormatter;
import org.phantazm.stats.zombies.ZombiesLeaderboardDatabase;
import org.phantazm.zombies.modifier.ModifierHandler;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

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
        @NotNull IdentitySource identitySource,
        @NotNull EventNode<Event> eventNode) {
    }

    @FactoryMethod
    public ZombiesBestTimeLeaderboard(@NotNull Data data, @NotNull @Child("tick_formatter") TickFormatter tickFormatter) {
        this.data = data;
        this.tickFormatter = tickFormatter;
    }

    @Override
    public @NotNull ZombiesBestTimeLeaderboard.Impl apply(@NotNull InjectionStore injectionStore, @NotNull Args args) {
        return new Impl(data, args.executor, args.instance, args.database, args.roleStore, args.identitySource, args.eventNode,
            tickFormatter);
    }

    public static class Impl implements Leaderboard {
        private static final Tag<Integer> PAGE_TAG =
            Tag.Integer("phantazm.zombies.lb.best_time.page").defaultValue(0);

        private static final Tag<Integer> TEAM_SIZE_INDEX =
            Tag.Integer("phantazm.zombies.lb.best_time.team_size").defaultValue(0);

        private static final Tag<Integer> MODIFIER_RANKING_INDEX =
            Tag.Integer("phantazm.zombies.lb.best_time.modifier_ranking").defaultValue(0);

        private final Data data;
        private final Executor executor;
        private final Instance instance;
        private final ZombiesLeaderboardDatabase database;
        private final RoleStore roleStore;
        private final IdentitySource identitySource;
        private final EventNode<Event> eventNode;
        private final TickFormatter tickFormatter;
        private final PaginatedHologram hologram;
        private final Object2IntMap<PageKey> pageMap;

        private record PageKey(int teamSize,
            ModifierRanking modifier) {
        }

        private Impl(Data data, Executor executor, Instance instance, ZombiesLeaderboardDatabase database, RoleStore roleStore,
            IdentitySource identitySource,
            EventNode<Event> eventNode,
            TickFormatter tickFormatter) {
            this.data = data;
            this.executor = executor;
            this.instance = instance;
            this.database = database;
            this.roleStore = roleStore;
            this.identitySource = identitySource;
            this.eventNode = eventNode;
            this.tickFormatter = tickFormatter;
            this.hologram = new BasicPaginatedHologram(VecUtils.toVec(data.location), PAGE_TAG);
            this.pageMap = new Object2IntOpenHashMap<>() {{
                defaultReturnValue(-1);
            }};

            data.teamSizeToEntryCountMappings.defaultReturnValue(12);

            initPages();
        }

        private void initPages() {
            IntIterator teamSizeIterator = data.teamSizes.intIterator();
            int i = 0;
            while (teamSizeIterator.hasNext()) {
                int teamSize = teamSizeIterator.nextInt();

                Component teamSizeName = data.teamSizeToNameMappings.get(teamSize);
                if (teamSizeName == null) {
                    continue;
                }

                for (ModifierRanking modifier : data.modifiers) {
                    TagResolver modifierNameTag = Placeholder.component("modifier_name", modifier.name);
                    TagResolver teamSizeNameTag = Placeholder.component("team_size_name", teamSizeName);

                    List<Component> subHeaders = new ArrayList<>(data.headerFormats.size());
                    for (String format : data.headerFormats) {
                        subHeaders.add(MiniMessage.miniMessage().deserialize(format, modifierNameTag, teamSizeNameTag));
                    }

                    for (String format : data.footerFormats) {
                        subHeaders.add(MiniMessage.miniMessage().deserialize(format, modifierNameTag, teamSizeNameTag));
                    }

                    List<Component> pageContents = new ArrayList<>(data.headerFormats.size() + subHeaders.size());
                    pageContents.addAll(subHeaders);

                    hologram.addPage(pageContents, 0, Hologram.Alignment.LOWER);
                    pageMap.put(new PageKey(teamSize, modifier), i++);
                }
            }
        }

        @SuppressWarnings("unchecked")
        private CompletableFuture<Void> renderTimesForPage(int teamSize, ModifierRanking modifier) {
            int page = pageMap.getInt(new PageKey(teamSize, modifier));
            if (page == -1) {
                return FutureUtils.nullCompletedFuture();
            }

            String descriptor = ModifierHandler.Global.instance().descriptor(modifier.modifiers);
            int entryCount = data.teamSizeToEntryCountMappings.get(teamSize);
            int entryStart = 0;
            return database.fetchBestTimes(teamSize, descriptor, data.map, entryStart, entryCount)
                .thenAcceptAsync(entries -> {
                    Component[] timeEntries = new Component[entries.size()];
                    CompletableFuture<?>[] entryFutures = new CompletableFuture[entries.size()];
                    for (int i = 0; i < entries.size(); i++) {
                        ZombiesLeaderboardDatabase.LeaderboardEntry entry = entries.get(i);

                        TagResolver rankTag = Placeholder.unparsed("rank", Integer.toString(entryStart + 1 + i));
                        TagResolver timeTag = Placeholder.unparsed("text", tickFormatter.format(entry.timeTaken()));

                        CompletableFuture<Component>[] names = new CompletableFuture[entry.team().size()];

                        List<UUID> shuffledTeam = new ArrayList<>(entry.team());
                        Collections.shuffle(shuffledTeam);

                        int j = 0;
                        for (UUID member : shuffledTeam) {
                            names[j++] = identitySource.getName(member).thenApplyAsync(nameOptional -> {
                                return nameOptional.orElse("Unknown");
                            }, executor).thenComposeAsync(name -> roleStore.getStylingRole(member).thenApplyAsync(role -> {
                                return role.styleRawName(name);
                            }, executor), executor);
                        }

                        int entryIndex = i;
                        entryFutures[i] = CompletableFuture.allOf(names).thenRunAsync(() -> {
                            List<Component> finishedNames = Arrays.stream(names)
                                .map(future -> future.getNow(null)).toList();

                            Component joinedNames = Component.join(JoinConfiguration.commas(true), finishedNames);

                            TagResolver namesTag = Placeholder.component("names", joinedNames);

                            timeEntries[entryIndex] = MiniMessage.miniMessage().deserialize(data.scoreFormat, rankTag,
                                timeTag, namesTag);
                        }, executor);
                    }

                    CompletableFuture.allOf(entryFutures).thenRunAsync(() -> {
                        hologram.updatePage(page, hologram -> {
                            int start = data.scoreInsertPoint;
                            int end = hologram.size() - data.footerFormats.size();
                            if (end >= start) {
                                hologram.subList(data.scoreInsertPoint, hologram.size() - data.footerFormats.size()).clear();
                            }

                            hologram.addAll(data.scoreInsertPoint, List.of(timeEntries));
                        });
                    }, executor);
                }, executor);
        }
    }

    public record ModifierRanking(@NotNull Set<Key> modifiers,
        @NotNull Component name) {
    }

    @DataObject
    public record Data(@NotNull Vec3D location,
        @NotNull @ChildPath("tick_formatter") String tickFormatter,
        @NotNull Key map,
        @NotNull IntSet teamSizes,
        @NotNull Set<ModifierRanking> modifiers,
        @NotNull Int2ObjectMap<Component> teamSizeToNameMappings,
        @NotNull Int2IntMap teamSizeToEntryCountMappings,
        @NotNull List<String> headerFormats,
        @NotNull List<String> footerFormats,
        int scoreInsertPoint,
        @NotNull String scoreFormat) {
    }
}
