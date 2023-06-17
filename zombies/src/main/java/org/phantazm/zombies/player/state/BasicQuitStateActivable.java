package org.phantazm.zombies.player.state;

import net.kyori.adventure.text.Component;
import net.minestom.server.instance.Instance;
import net.minestom.server.scoreboard.Sidebar;
import net.minestom.server.scoreboard.TabList;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Activable;
import org.phantazm.commons.CancellableState;
import org.phantazm.core.player.PlayerView;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class BasicQuitStateActivable implements Activable {

    private final Instance instance;

    private final PlayerView playerView;

    private final Sidebar sidebar;

    private final TabList tabList;

    private final Map<UUID, CancellableState> stateMap;

    public BasicQuitStateActivable(@NotNull Instance instance, @NotNull PlayerView playerView, @NotNull Sidebar sidebar,
            @NotNull TabList tabList, @NotNull Map<UUID, CancellableState> stateMap) {
        this.instance = Objects.requireNonNull(instance, "instance");
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.sidebar = Objects.requireNonNull(sidebar, "sidebar");
        this.tabList = Objects.requireNonNull(tabList, "tabList");
        this.stateMap = Objects.requireNonNull(stateMap, "stateMap");
    }

    @Override
    public void start() {
        playerView.getPlayer().ifPresent(player -> {
            player.getInventory().clear();
            player.setLevel(0);
            player.setExp(0);
            sidebar.removeViewer(player);
            tabList.removeViewer(player);
        });
        playerView.getDisplayName()
                .thenAccept(displayName -> instance.sendMessage(displayName.append(Component.text(" quit."))));

        for (CancellableState state : stateMap.values()) {
            state.end();
        }

        stateMap.clear();
    }

}
