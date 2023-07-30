package org.phantazm.zombies.player.state;

import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.instance.Instance;
import net.minestom.server.scoreboard.BelowNameTag;
import net.minestom.server.scoreboard.Sidebar;
import net.minestom.server.scoreboard.TabList;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Activable;
import org.phantazm.commons.CancellableState;
import org.phantazm.commons.TickTaskScheduler;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.map.MapSettingsInfo;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class BasicQuitStateActivable implements Activable {
    private final Instance instance;
    private final PlayerView playerView;
    private final MapSettingsInfo settings;
    private final Sidebar sidebar;
    private final TabList tabList;
    private final BelowNameTag belowNameTag;
    private final Map<UUID, CancellableState> stateMap;
    private final TickTaskScheduler scheduler;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public BasicQuitStateActivable(@NotNull Instance instance, @NotNull PlayerView playerView,
            @NotNull MapSettingsInfo settings, @NotNull Sidebar sidebar, @NotNull TabList tabList,
            @NotNull BelowNameTag belowNameTag,
            @NotNull Map<UUID, CancellableState> stateMap, @NotNull TickTaskScheduler scheduler) {
        this.instance = Objects.requireNonNull(instance, "instance");
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.sidebar = Objects.requireNonNull(sidebar, "sidebar");
        this.tabList = Objects.requireNonNull(tabList, "tabList");
        this.belowNameTag = Objects.requireNonNull(belowNameTag, "belowNameTag");
        this.stateMap = Objects.requireNonNull(stateMap, "stateMap");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    }

    @Override
    public void start() {
        playerView.getPlayer().ifPresent(player -> {
            player.getInventory().clear();
            player.setLevel(0);
            player.setExp(0);
            sidebar.removeViewer(player);
            tabList.removeViewer(player);
            belowNameTag.removeViewer(player);
            player.setHealth(player.getMaxHealth());
            player.resetTitle();
            player.sendActionBar(Component.empty());
            player.stopSound(SoundStop.all());
        });
        playerView.getDisplayName().thenAccept(displayName -> {
            TagResolver quitterPlaceholder = Placeholder.component("quitter", displayName);
            instance.sendMessage(miniMessage.deserialize(settings.quitMessageFormat(), quitterPlaceholder));
        });

        for (CancellableState state : stateMap.values()) {
            state.end();
        }

        scheduler.end();
        stateMap.clear();
    }
}
