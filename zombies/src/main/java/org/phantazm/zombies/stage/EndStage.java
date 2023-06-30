package org.phantazm.zombies.stage;

import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.TitlePart;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.time.TickFormatter;
import org.phantazm.stats.zombies.ZombiesPlayerMapStats;
import org.phantazm.zombies.map.MapSettingsInfo;
import org.phantazm.zombies.map.handler.RoundHandler;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;
import org.phantazm.zombies.player.state.context.DeadPlayerStateContext;
import org.phantazm.zombies.sidebar.SidebarUpdater;

import java.util.*;
import java.util.function.Function;

public class EndStage implements Stage {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final Instance instance;
    private final MapSettingsInfo settings;
    private final TickFormatter tickFormatter;
    private final Collection<? extends ZombiesPlayer> zombiesPlayers;

    private final Wrapper<Long> remainingTicks;
    private final Wrapper<Long> ticksSinceStart;

    private final Function<? super ZombiesPlayer, ? extends SidebarUpdater> sidebarUpdaterCreator;
    private final RoundHandler roundHandler;

    private final Map<UUID, SidebarUpdater> sidebarUpdaters;

    private boolean hasWon;

    public EndStage(@NotNull Instance instance, @NotNull MapSettingsInfo settings, @NotNull TickFormatter tickFormatter,
            @NotNull Collection<? extends ZombiesPlayer> zombiesPlayers, @NotNull Wrapper<Long> remainingTicks,
            @NotNull Wrapper<Long> ticksSinceStart,
            @NotNull Function<? super ZombiesPlayer, ? extends SidebarUpdater> sidebarUpdaterCreator,
            @NotNull RoundHandler roundHandler) {
        this.instance = Objects.requireNonNull(instance, "instance");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.tickFormatter = Objects.requireNonNull(tickFormatter, "tickFormatter");
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
        this.remainingTicks = Objects.requireNonNull(remainingTicks, "remainingTicks");
        this.ticksSinceStart = Objects.requireNonNull(ticksSinceStart, "ticksSinceStart");
        this.sidebarUpdaterCreator = Objects.requireNonNull(sidebarUpdaterCreator, "sidebarUpdaterCreator");
        this.roundHandler = Objects.requireNonNull(roundHandler, "roundHandler");

        this.sidebarUpdaters = new HashMap<>();
    }

    public long ticksSinceStart() {
        return ticksSinceStart.get();
    }

    @Override
    public boolean shouldContinue() {
        return remainingTicks.get() == 0L;
    }

    @Override
    public boolean shouldRevert() {
        return false;
    }

    @Override
    public boolean shouldAbort() {
        return false;
    }

    @Override
    public void onJoin(@NotNull ZombiesPlayer zombiesPlayer) {
        zombiesPlayer.module().getMeta().setInGame(false);
    }

    @Override
    public void onLeave(@NotNull ZombiesPlayer zombiesPlayer) {

    }

    @Override
    public boolean hasPermanentPlayers() {
        return true;
    }

    @Override
    public boolean canRejoin() {
        return false;
    }

    @Override
    public void start() {
        instance.playSound(Sound.sound(SoundEvent.ENTITY_ENDER_DRAGON_DEATH, Sound.Source.MASTER, 1.0F, 1.0F));

        boolean anyAlive = false;
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            if (zombiesPlayer.isAlive()) {
                anyAlive = true;
                break;
            }
        }

        Component finalTime = Component.text(tickFormatter.format(ticksSinceStart.get()));
        int bestRound = roundHandler.currentRoundIndex();

        TagResolver roundPlaceholder = Placeholder.component("round", Component.text(bestRound + 1));
        if (anyAlive) {
            instance.sendTitlePart(TitlePart.TITLE,
                    MINI_MESSAGE.deserialize(settings.winTitleFormat(), roundPlaceholder));
            instance.sendTitlePart(TitlePart.SUBTITLE,
                    MINI_MESSAGE.deserialize(settings.winSubtitleFormat(), roundPlaceholder));

            for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
                ZombiesPlayerMapStats stats = zombiesPlayer.module().getStats();
                stats.setWins(stats.getWins() + 1);
                stats.getBestTime().ifPresentOrElse(prevBest -> {
                    if (ticksSinceStart.get() < prevBest) {
                        stats.setBestTime(ticksSinceStart.get());
                    }
                }, () -> stats.setBestTime(ticksSinceStart.get()));
            }

            this.hasWon = true;
        }
        else {
            instance.sendTitlePart(TitlePart.TITLE,
                    MINI_MESSAGE.deserialize(settings.lossTitleFormat(), roundPlaceholder));
            instance.sendTitlePart(TitlePart.SUBTITLE,
                    MINI_MESSAGE.deserialize(settings.lossSubtitleFormat(), roundPlaceholder));

            this.hasWon = false;
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
                    Placeholder.component("final_time", finalTime), roundPlaceholder,
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

            zombiesPlayer.sendMessage(MINI_MESSAGE.deserialize(settings.endGameStatsFormat(), tagResolvers));
        }

        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            zombiesPlayer.module().getMeta().setInGame(false);

            if (zombiesPlayer.isState(ZombiesPlayerStateKeys.KNOCKED)) {
                zombiesPlayer.setState(ZombiesPlayerStateKeys.DEAD, DeadPlayerStateContext.killed(null, null));
            }
        }
    }

    @Override
    public void tick(long time) {
        remainingTicks.apply(ticks -> ticks - 1);
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            if (!zombiesPlayer.hasQuit()) {
                SidebarUpdater sidebarUpdater = sidebarUpdaters.computeIfAbsent(zombiesPlayer.getUUID(),
                        unused -> sidebarUpdaterCreator.apply(zombiesPlayer));
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
        for (SidebarUpdater sidebarUpdater : sidebarUpdaters.values()) {
            sidebarUpdater.end();
        }
    }

    @Override
    public @NotNull Key key() {
        return StageKeys.END;
    }

    public boolean hasWon() {
        return hasWon;
    }
}
