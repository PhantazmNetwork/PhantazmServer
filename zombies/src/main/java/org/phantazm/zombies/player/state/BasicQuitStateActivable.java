package org.phantazm.zombies.player.state;

import net.kyori.adventure.text.Component;
import net.minestom.server.instance.Instance;
import net.minestom.server.scoreboard.Sidebar;
import net.minestom.server.scoreboard.TabList;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Activable;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.ZombiesPlayerMeta;

import java.util.Collection;
import java.util.Objects;

public class BasicQuitStateActivable implements Activable {

    private final Instance instance;

    private final PlayerView playerView;

    private final Sidebar sidebar;

    private final TabList tabList;

    public BasicQuitStateActivable(@NotNull Instance instance, @NotNull PlayerView playerView, @NotNull Sidebar sidebar,
            @NotNull TabList tabList) {
        this.instance = Objects.requireNonNull(instance, "instance");
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.sidebar = Objects.requireNonNull(sidebar, "sidebar");
        this.tabList = Objects.requireNonNull(tabList, "tabList");
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
    }

}
