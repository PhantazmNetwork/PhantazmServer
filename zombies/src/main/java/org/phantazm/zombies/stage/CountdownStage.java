package org.phantazm.zombies.stage;

import com.github.steanky.toolkit.collection.Wrapper;
import it.unimi.dsi.fastutil.longs.LongList;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.TitlePart;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.chat.MessageWithDestination;
import org.phantazm.core.time.TickFormatter;
import org.phantazm.zombies.map.MapSettingsInfo;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.sidebar.SidebarUpdater;

import java.util.*;
import java.util.function.Function;

public class CountdownStage implements Stage {
    private final Instance instance;

    private final Collection<? extends ZombiesPlayer> zombiesPlayers;

    private final MapSettingsInfo settings;

    private final Map<UUID, SidebarUpdater> sidebarUpdaters = new HashMap<>();

    private final Random random;

    private final Wrapper<Long> ticksRemaining;

    private final long initialTicks;

    private final LongList alertTicks;

    private final TickFormatter tickFormatter;

    private final Function<? super ZombiesPlayer, ? extends SidebarUpdater> sidebarUpdaterCreator;

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    protected CountdownStage(@NotNull Instance instance, @NotNull Collection<? extends ZombiesPlayer> zombiesPlayers,
        @NotNull MapSettingsInfo settings, @NotNull Random random, @NotNull Wrapper<Long> ticksRemaining,
        @NotNull LongList alertTicks, @NotNull TickFormatter tickFormatter,
        @NotNull Function<? super ZombiesPlayer, ? extends SidebarUpdater> sidebarUpdaterCreator) {
        this.instance = Objects.requireNonNull(instance);
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers);
        this.settings = Objects.requireNonNull(settings);
        this.random = Objects.requireNonNull(random);
        this.ticksRemaining = Objects.requireNonNull(ticksRemaining);
        this.initialTicks = ticksRemaining.get();
        this.alertTicks = Objects.requireNonNull(alertTicks);
        this.tickFormatter = Objects.requireNonNull(tickFormatter);
        this.sidebarUpdaterCreator = Objects.requireNonNull(sidebarUpdaterCreator);
    }

    public CountdownStage(@NotNull Instance instance, @NotNull Collection<? extends ZombiesPlayer> zombiesPlayers,
        @NotNull MapSettingsInfo settings, @NotNull Random random, long countdownTicks,
        @NotNull LongList alertTicks, @NotNull TickFormatter tickFormatter,
        @NotNull Function<? super ZombiesPlayer, ? extends SidebarUpdater> sidebarUpdaterCreator) {
        this(instance, zombiesPlayers, settings, random, Wrapper.of(countdownTicks), alertTicks, tickFormatter,
            sidebarUpdaterCreator);
    }

    @Override
    public boolean shouldContinue() {
        return ticksRemaining.get() == 0L;
    }

    @Override
    public boolean shouldRevert() {
        return zombiesPlayers.isEmpty();
    }

    @Override
    public boolean shouldAbort() {
        return false;
    }

    @Override
    public void onJoin(@NotNull ZombiesPlayer zombiesPlayer) {
        zombiesPlayer.getPlayer().ifPresent(player -> {
            if (settings.introMessages().isEmpty()) {
                return;
            }

            List<MessageWithDestination> messages = settings.introMessages()
                .get(random.nextInt(settings.introMessages().size()));
            for (MessageWithDestination message : messages) {
                switch (message.destination()) {
                    case TITLE -> player.sendTitlePart(TitlePart.TITLE, message.component());
                    case SUBTITLE -> player.sendTitlePart(TitlePart.SUBTITLE, message.component());
                    case CHAT -> player.sendMessage(message.component());
                    case ACTION_BAR -> player.sendActionBar(message.component());
                }
            }
        });

        int count = zombiesPlayers.size(), maxPlayers = settings.maxPlayers();
        TagResolver countPlaceholder = Placeholder.component("count", Component.text(count));
        TagResolver maxPlayersPlaceholder = Placeholder.component("max_players", Component.text(maxPlayers));
        zombiesPlayer.module().getPlayerView().getDisplayName().thenAccept(displayName -> {
            TagResolver joinerPlaceholder = Placeholder.component("joiner", displayName);
            Component message = MiniMessage.miniMessage()
                .deserialize(settings.gameJoinFormat(), joinerPlaceholder, countPlaceholder,
                    maxPlayersPlaceholder);
            instance.sendMessage(message);
        });
    }

    @Override
    public void onLeave(@NotNull ZombiesPlayer zombiesPlayer) {
        SidebarUpdater updater = sidebarUpdaters.remove(zombiesPlayer.getUUID());
        if (updater != null) {
            updater.end();
        }
    }

    @Override
    public boolean hasPermanentPlayers() {
        return false;
    }

    @Override
    public boolean canRejoin() {
        return false;
    }

    @Override
    public boolean canJoin() {
        return true;
    }

    @Override
    public boolean preventsShutdown() {
        return false;
    }

    @Override
    public void start() {
        ticksRemaining.set(initialTicks);
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            if (zombiesPlayer.hasQuit()) {
                continue;
            }

            zombiesPlayer.getPlayer().ifPresent(player -> {
                if (settings.introMessages().isEmpty()) {
                    return;
                }

                List<MessageWithDestination> messages = settings.introMessages()
                    .get(random.nextInt(settings.introMessages().size()));
                for (MessageWithDestination message : messages) {
                    switch (message.destination()) {
                        case TITLE -> player.sendTitlePart(TitlePart.TITLE, message.component());
                        case SUBTITLE -> player.sendTitlePart(TitlePart.SUBTITLE, message.component());
                        case CHAT -> player.sendMessage(message.component());
                        case ACTION_BAR -> player.sendActionBar(message.component());
                    }
                }
            });
        }
    }

    @Override
    public void tick(long time) {
        if (alertTicks.contains((long) ticksRemaining.get())) {
            TagResolver timePlaceholder = Placeholder.unparsed("time", tickFormatter.format(ticksRemaining.get()));
            Component message = miniMessage.deserialize(settings.countdownTimeFormat(), timePlaceholder);
            instance.playSound(settings.countdownTickSound());
            instance.sendMessage(message);
        }
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            if (!zombiesPlayer.hasQuit()) {
                SidebarUpdater sidebarUpdater = sidebarUpdaters.computeIfAbsent(zombiesPlayer.getUUID(),
                    unused -> sidebarUpdaterCreator.apply(zombiesPlayer));
                sidebarUpdater.tick(time);
            }
        }

        ticksRemaining.apply(ticks -> ticks - 1);
    }

    @Override
    public void end() {
        for (SidebarUpdater sidebarUpdater : sidebarUpdaters.values()) {
            sidebarUpdater.end();
        }
    }

    @Override
    public @NotNull Key key() {
        return StageKeys.COUNTDOWN;
    }
}
