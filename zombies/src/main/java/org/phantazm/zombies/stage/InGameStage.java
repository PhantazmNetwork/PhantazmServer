package org.phantazm.zombies.stage;

import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.TitlePart;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.EquipmentHandler;
import org.phantazm.core.time.TickFormatter;
import org.phantazm.zombies.map.MapSettingsInfo;
import org.phantazm.zombies.map.handler.RoundHandler;
import org.phantazm.zombies.map.handler.ShopHandler;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.ZombiesPlayerModule;
import org.phantazm.zombies.sidebar.SidebarUpdater;
import org.phantazm.stats.zombies.ZombiesPlayerMapStats;

import java.util.*;
import java.util.function.Function;

public class InGameStage implements Stage {
    private final Instance instance;
    private final Collection<? extends ZombiesPlayer> zombiesPlayers;
    private final MapSettingsInfo settings;
    private final Map<UUID, SidebarUpdater> sidebarUpdaters = new HashMap<>();
    private final Pos spawnPos;
    private final RoundHandler roundHandler;
    private final Wrapper<Long> ticksSinceStart;
    private final Map<Key, List<Key>> defaultEquipment;
    private final Set<Key> equipmentGroups;
    private final Function<? super ZombiesPlayer, ? extends SidebarUpdater> sidebarUpdaterCreator;
    private final ShopHandler shopHandler;
    private final TickFormatter tickFormatter;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public InGameStage(@NotNull Instance instance, @NotNull Collection<? extends ZombiesPlayer> zombiesPlayers,
            @NotNull MapSettingsInfo settings, @NotNull Pos spawnPos, @NotNull RoundHandler roundHandler,
            @NotNull Wrapper<Long> ticksSinceStart, @NotNull Map<Key, List<Key>> defaultEquipment,
            @NotNull Set<Key> equipmentGroups,
            @NotNull Function<? super ZombiesPlayer, ? extends SidebarUpdater> sidebarUpdaterCreator,
            @NotNull ShopHandler shopHandler, @NotNull TickFormatter endTimeTickFormatter) {
        this.instance = Objects.requireNonNull(instance, "instance");
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.spawnPos = Objects.requireNonNull(spawnPos, "spawnPos");
        this.roundHandler = Objects.requireNonNull(roundHandler, "roundHandler");
        this.ticksSinceStart = Objects.requireNonNull(ticksSinceStart, "ticksSinceStart");
        this.defaultEquipment = Objects.requireNonNull(defaultEquipment, "defaultEquipment");
        this.equipmentGroups = Objects.requireNonNull(equipmentGroups, "equipmentGroups");
        this.sidebarUpdaterCreator = Objects.requireNonNull(sidebarUpdaterCreator, "sidebarUpdaterCreator");
        this.shopHandler = Objects.requireNonNull(shopHandler, "shopHandler");
        this.tickFormatter = Objects.requireNonNull(endTimeTickFormatter, "tickFormatter");
    }

    public long ticksSinceStart() {
        return ticksSinceStart.get();
    }

    @Override
    public boolean shouldContinue() {
        if (roundHandler.hasEnded()) {
            return true;
        }

        boolean anyAlive = false;
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            if (zombiesPlayer.isAlive()) {
                anyAlive = true;
                break;
            }
        }
        return !anyAlive;
    }

    @Override
    public boolean shouldRevert() {
        return false;
    }

    @Override
    public void onJoin(@NotNull ZombiesPlayer zombiesPlayer) {
        zombiesPlayer.module().getMeta().setInGame(true);
    }

    @Override
    public void onLeave(@NotNull ZombiesPlayer zombiesPlayer) {

    }

    @Override
    public boolean hasPermanentPlayers() {
        return true;
    }

    @Override
    public void start() {
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            zombiesPlayer.module().getMeta().setInGame(true);
            zombiesPlayer.module().getStats().setGamesPlayed(zombiesPlayer.module().getStats().getGamesPlayed() + 1);
            zombiesPlayer.getPlayer().ifPresent(player -> {
                player.teleport(spawnPos);
            });

            ZombiesPlayerModule module = zombiesPlayer.module();
            EquipmentHandler equipmentHandler = module.getEquipmentHandler();
            for (Key groupKey : defaultEquipment.keySet()) {
                if (!equipmentHandler.canAddEquipment(groupKey)) {
                    continue;
                }

                for (Key key : defaultEquipment.get(groupKey)) {
                    module.getEquipmentCreator().createEquipment(key)
                            .ifPresent(equipment -> equipmentHandler.addEquipment(equipment, groupKey));

                    if (!equipmentHandler.canAddEquipment(groupKey)) {
                        break;
                    }
                }
            }

            for (Key group : equipmentGroups) {
                equipmentHandler.refreshGroup(group);
            }
        }

        shopHandler.initialize();
        ticksSinceStart.set(0L);
        roundHandler.setCurrentRound(0);
    }

    @Override
    public void tick(long time) {
        ticksSinceStart.apply(ticks -> ticks + 1);
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            if (!zombiesPlayer.hasQuit()) {
                SidebarUpdater sidebarUpdater = sidebarUpdaters.computeIfAbsent(zombiesPlayer.getUUID(), unused -> {
                    return sidebarUpdaterCreator.apply(zombiesPlayer);
                });
                sidebarUpdater.tick(time);
            }

            zombiesPlayer.getPlayer().ifPresent(player -> {
                for (ZombiesPlayer otherPlayer : zombiesPlayers) {
                    otherPlayer.module().getTabList().updateScore(player, zombiesPlayer.module().getKills().getKills());
                }
            });
        }
    }

    @Override
    public void end() {
        boolean anyAlive = false;
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            if (zombiesPlayer.isAlive()) {
                anyAlive = true;
                break;
            }
        }

        Component finalTime = Component.text(tickFormatter.format(ticksSinceStart.get()));
        int bestRound = Math.min(roundHandler.currentRoundIndex() + 1, roundHandler.roundCount());

        TagResolver roundPlaceholder = Placeholder.component("round", Component.text(bestRound));
        if (anyAlive) {
            instance.sendTitlePart(TitlePart.TITLE,
                    miniMessage.deserialize(settings.winTitleFormat(), roundPlaceholder));
            instance.sendTitlePart(TitlePart.SUBTITLE,
                    miniMessage.deserialize(settings.winSubtitleFormat(), roundPlaceholder));

            for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
                ZombiesPlayerMapStats stats = zombiesPlayer.module().getStats();
                stats.setWins(stats.getWins() + 1);
                stats.getBestTime().ifPresentOrElse(prevBest -> {
                    if (ticksSinceStart.get() < prevBest) {
                        stats.setBestTime(ticksSinceStart.get());
                    }
                }, () -> stats.setBestTime(ticksSinceStart.get()));
            }
        }
        else {
            instance.sendTitlePart(TitlePart.TITLE,
                    miniMessage.deserialize(settings.lossTitleFormat(), roundPlaceholder));
            instance.sendTitlePart(TitlePart.SUBTITLE,
                    miniMessage.deserialize(settings.lossSubtitleFormat(), roundPlaceholder));
        }

        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            if (zombiesPlayer.hasQuit()) {
                continue;
            }

            ZombiesPlayerMapStats stats = zombiesPlayer.module().getStats();
            int gunAccuracy = stats.getShots() <= 0
                              ? 100
                              : (int)Math.rint(((double)(stats.getRegularHits() + stats.getHeadshotHits()) /
                                      (double)stats.getShots()) * 100);
            int headshotAccuracy = stats.getShots() <= 0
                                   ? 100
                                   : (int)Math.rint(
                                           ((double)(stats.getHeadshotHits()) / (double)stats.getShots()) * 100);
            TagResolver[] tagResolvers = new TagResolver[] {Placeholder.component("map", settings.displayName()),
                    Placeholder.component("final_time", finalTime),
                    Placeholder.component("best_round", Component.text(bestRound)),
                    Placeholder.component("total_shots", Component.text(stats.getShots())),
                    Placeholder.component("regular_hits", Component.text(stats.getRegularHits())),
                    Placeholder.component("headshot_hits", Component.text(stats.getHeadshotHits())),
                    Placeholder.component("gun_accuracy", Component.text(gunAccuracy)),
                    Placeholder.component("headshot_accuracy", Component.text(headshotAccuracy)),
                    Placeholder.component("kills", Component.text(stats.getKills())),
                    Placeholder.component("coins_gained", Component.text(stats.getCoinsGained())),
                    Placeholder.component("coins_spent", Component.text(stats.getCoinsSpent())),
                    Placeholder.component("knocks", Component.text(stats.getKnocks())),
                    Placeholder.component("deaths", Component.text(stats.getDeaths())),
                    Placeholder.component("revives", Component.text(stats.getRevives()))};

            zombiesPlayer.sendMessage(miniMessage.deserialize(settings.endGameStatsFormat(), tagResolvers));
        }

        for (SidebarUpdater sidebarUpdater : sidebarUpdaters.values()) {
            sidebarUpdater.end();
        }
    }

    @Override
    public @NotNull Key key() {
        return StageKeys.IN_GAME;
    }
}
