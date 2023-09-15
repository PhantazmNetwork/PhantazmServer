package org.phantazm.zombies.stage;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.MapSettingsInfo;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.sidebar.SidebarUpdater;

import java.util.*;
import java.util.function.Function;

public class IdleStage implements Stage {

    private final Map<UUID, SidebarUpdater> sidebarUpdaters = new HashMap<>();

    private final Instance instance;

    private final MapSettingsInfo settings;

    private final Collection<? extends ZombiesPlayer> zombiesPlayers;

    private final Function<? super ZombiesPlayer, ? extends SidebarUpdater> sidebarUpdaterCreator;

    private final long revertTicks;

    private long emptyTicks;

    public IdleStage(@NotNull Instance instance, @NotNull MapSettingsInfo settings,
        @NotNull Collection<? extends ZombiesPlayer> zombiesPlayers,
        @NotNull Function<? super ZombiesPlayer, ? extends SidebarUpdater> sidebarUpdaterCreator,
        long revertTicks) {
        this.instance = Objects.requireNonNull(instance);
        this.settings = Objects.requireNonNull(settings);
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers);
        this.sidebarUpdaterCreator = Objects.requireNonNull(sidebarUpdaterCreator);
        this.revertTicks = revertTicks;
    }

    @Override
    public boolean shouldContinue() {
        return !zombiesPlayers.isEmpty();
    }

    @Override
    public boolean shouldRevert() {
        return false;
    }

    @Override
    public boolean shouldAbort() {
        return emptyTicks >= revertTicks;
    }

    @Override
    public void onJoin(@NotNull ZombiesPlayer zombiesPlayer) {
        zombiesPlayer.module().getLeaderboard().startIfNotActive();
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

        zombiesPlayer.module().getLeaderboard().endIfActive();
    }

    @Override
    public void start() {
        emptyTicks = 0L;
    }

    @Override
    public void tick(long time) {
        if (zombiesPlayers.isEmpty()) {
            ++emptyTicks;
        } else {
            emptyTicks = 0L;

            for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
                if (!zombiesPlayer.hasQuit()) {
                    SidebarUpdater sidebarUpdater = sidebarUpdaters.computeIfAbsent(zombiesPlayer.getUUID(), unused -> {
                        return sidebarUpdaterCreator.apply(zombiesPlayer);
                    });
                    sidebarUpdater.tick(time);
                }
            }
        }
    }

    @Override
    public void end() {
        for (SidebarUpdater sidebarUpdater : sidebarUpdaters.values()) {
            sidebarUpdater.end();
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
    public @NotNull Key key() {
        return StageKeys.IDLE_STAGE;
    }
}
