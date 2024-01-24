package org.phantazm.zombies.stage;

import com.github.steanky.toolkit.collection.Wrapper;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.TitlePart;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.packet.MinestomPacketUtils;
import org.phantazm.core.time.TickFormatter;
import org.phantazm.messaging.packet.server.RoundStartPacket;
import org.phantazm.stats.zombies.ZombiesLeaderboardDatabase;
import org.phantazm.stats.zombies.ZombiesPlayerMapStats;
import org.phantazm.zombies.map.MapSettingsInfo;
import org.phantazm.zombies.map.WebhookInfo;
import org.phantazm.zombies.map.handler.RoundHandler;
import org.phantazm.zombies.modifier.ModifierComponent;
import org.phantazm.zombies.modifier.ModifierUtils;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;
import org.phantazm.zombies.player.state.context.DeadPlayerStateContext;
import org.phantazm.zombies.scene2.ZombiesScene;
import org.phantazm.zombies.sidebar.SidebarUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class EndStage implements Stage {
    private static final Logger LOGGER = LoggerFactory.getLogger(EndStage.class);
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final Instance instance;
    private final MapSettingsInfo settings;
    private final WebhookInfo webhook;
    private final TickFormatter tickFormatter;
    private final Collection<? extends ZombiesPlayer> zombiesPlayers;

    private final Wrapper<Long> remainingTicks;
    private final Wrapper<Long> ticksSinceStart;

    private final BiFunction<? super ZombiesPlayer, Boolean, ? extends SidebarUpdater> sidebarUpdaterCreator;
    private final RoundHandler roundHandler;
    private final ZombiesLeaderboardDatabase leaderboardDatabase;
    private final Supplier<ZombiesScene> sceneSupplier;

    private final Map<UUID, SidebarUpdater> sidebarUpdaters;
    private final HttpClient client = HttpClient.newHttpClient();

    private boolean hasWon;

    public EndStage(@NotNull Instance instance, @NotNull MapSettingsInfo settings, @NotNull WebhookInfo webhook,
        @NotNull TickFormatter tickFormatter, @NotNull Collection<? extends ZombiesPlayer> zombiesPlayers,
        @NotNull Wrapper<Long> remainingTicks, @NotNull Wrapper<Long> ticksSinceStart,
        @NotNull BiFunction<? super ZombiesPlayer, Boolean, ? extends SidebarUpdater> sidebarUpdaterCreator,
        @NotNull RoundHandler roundHandler, @NotNull ZombiesLeaderboardDatabase leaderboardDatabase,
        @NotNull Supplier<ZombiesScene> sceneSupplier) {
        this.instance = Objects.requireNonNull(instance);
        this.settings = Objects.requireNonNull(settings);
        this.webhook = Objects.requireNonNull(webhook);
        this.tickFormatter = Objects.requireNonNull(tickFormatter);
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers);
        this.remainingTicks = Objects.requireNonNull(remainingTicks);
        this.ticksSinceStart = Objects.requireNonNull(ticksSinceStart);
        this.sidebarUpdaterCreator = Objects.requireNonNull(sidebarUpdaterCreator);
        this.roundHandler = Objects.requireNonNull(roundHandler);
        this.leaderboardDatabase = Objects.requireNonNull(leaderboardDatabase);
        this.sceneSupplier = Objects.requireNonNull(sceneSupplier);

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
    public boolean canJoin() {
        return false;
    }

    @Override
    public boolean preventsShutdown() {
        return true;
    }

    @Override
    public void start() {
        instance.playSound(Sound.sound(SoundEvent.ENTITY_ENDER_DRAGON_DEATH, Sound.Source.MASTER, 1.0F, 1.0F));

        ZombiesScene scene = this.sceneSupplier.get();
        boolean isLegit = scene.isLegit() && ticksSinceStart.get() > 20;
        boolean anyAlive = false;
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            if (zombiesPlayer.isAlive()) {
                anyAlive = true;
                break;
            }
        }

        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            zombiesPlayer.module().getMeta().setInGame(false);

            if (zombiesPlayer.isState(ZombiesPlayerStateKeys.KNOCKED)) {
                Point deathLocation = zombiesPlayer.getPlayer().map(Player::getPosition).orElse(null);
                zombiesPlayer.setState(ZombiesPlayerStateKeys.DEAD,
                    DeadPlayerStateContext.killed(deathLocation, null, null));
            }
        }

        String timeString = tickFormatter.format(ticksSinceStart.get());
        Component finalTime = Component.text(timeString);
        int bestRound = roundHandler.currentRoundIndex();

        TagResolver roundPlaceholder = Placeholder.component("round", Component.text(bestRound + 1));
        if (anyAlive) {
            instance.sendTitlePart(TitlePart.TITLE,
                MINI_MESSAGE.deserialize(settings.winTitleFormat(), roundPlaceholder));
            instance.sendTitlePart(TitlePart.SUBTITLE,
                MINI_MESSAGE.deserialize(settings.winSubtitleFormat(), roundPlaceholder));

            if (isLegit && settings.trackStats()) {
                leaderboardDatabase.submitGame(zombiesPlayers.stream()
                        .map(player -> player.module().getPlayerView().getUUID()).collect(Collectors.toSet()),
                    ModifierUtils.modifierDescriptor(scene.activeModifiers()), scene.mapSettingsInfo().id(),
                    ticksSinceStart.get(), Instant.now().getEpochSecond());
            }

            for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
                if (isLegit && settings.trackStats()) {
                    ZombiesPlayerMapStats stats = zombiesPlayer.module().getStats();
                    stats.setWins(stats.getWins() + 1);
                }

                zombiesPlayer.getPlayer().ifPresent(player -> {
                    byte[] data = MinestomPacketUtils.serialize(new RoundStartPacket());
                    player.sendPluginMessage(RoundStartPacket.ID.asString(), data);
                });
            }

            this.hasWon = true;

            if (isLegit) {
                runWebhook(timeString);
            }
        } else {
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
                : (int) Math.rint(((double) (stats.getRegularHits() + stats.getHeadshotHits()) /
                (double) stats.getShots()) * 100);
            int headshotAccuracy = stats.getShots() <= 0
                ? 100
                : (int) Math.rint(
                ((double) (stats.getHeadshotHits()) / (double) stats.getShots()) * 100);
            TagResolver[] tagResolvers = new TagResolver[]{Placeholder.component("map", settings.displayName()),
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
    }

    private void runWebhook(String time) {
        if (!webhook.enabled()) {
            return;
        }

        try {
            List<CompletableFuture<String>> futures = new ArrayList<>(zombiesPlayers.size());
            IntList kills = new IntArrayList();
            for (ZombiesPlayer player : zombiesPlayers) {
                futures.add(player.module().getPlayerView().getUsername());
                kills.add(player.module().getKills().getKills());
            }

            String date = "<t:" + Instant.now().getEpochSecond() + ":f>";
            String modifiers;
            ZombiesScene zombiesScene = this.sceneSupplier.get();
            Set<ModifierComponent> activeModifiers = zombiesScene.activeModifiers();
            if (activeModifiers.isEmpty()) {
                modifiers = webhook.noModifierPlaceholder();
            } else {
                modifiers = activeModifiers.stream().map(ModifierComponent::webhookEmoji).collect(Collectors.joining());
            }

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenCompose(ignored -> {
                List<String> formattedUsernames = new ArrayList<>(futures.size());
                for (int i = 0; i < futures.size(); i++) {
                    formattedUsernames.add(
                        MessageFormat.format(webhook.playerFormat(), futures.get(i).join().replaceAll("_", "\\\\\\\\_"),
                            kills.getInt(i)));
                }

                String playerList = String.join(", ", formattedUsernames);
                String output = MessageFormat.format(webhook.webhookFormat(), date, time, zombiesPlayers.size(),
                    playerList, modifiers);
                HttpRequest request = HttpRequest.newBuilder(URI.create(webhook.webhookURL()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(output))
                    .build();
                return client.sendAsync(request, HttpResponse.BodyHandlers.discarding());
            }).whenComplete((ignored, throwable) -> {
                if (throwable != null) {
                    LOGGER.warn("Failed to send webhook", throwable);
                }
            });
        } catch (Exception e) {
            LOGGER.warn("Failed to send webhook", e);
        }
    }

    @Override
    public void tick(long time) {
        remainingTicks.apply(ticks -> ticks - 1);
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            if (!zombiesPlayer.hasQuit()) {
                SidebarUpdater sidebarUpdater = sidebarUpdaters.computeIfAbsent(zombiesPlayer.getUUID(),
                    unused -> sidebarUpdaterCreator.apply(zombiesPlayer, hasWon));
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
