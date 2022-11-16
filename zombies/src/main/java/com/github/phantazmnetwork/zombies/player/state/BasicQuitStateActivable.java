package com.github.phantazmnetwork.zombies.player.state;

import com.github.phantazmnetwork.commons.Activable;
import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayerMeta;
import net.kyori.adventure.text.Component;
import net.minestom.server.instance.Instance;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BasicQuitStateActivable implements Activable {

    private final Instance instance;

    private final PlayerView playerView;

    private final ZombiesPlayerMeta meta;

    private final Sidebar sidebar;

    public BasicQuitStateActivable(@NotNull Instance instance, @NotNull PlayerView playerView,
            @NotNull ZombiesPlayerMeta meta, @NotNull Sidebar sidebar) {
        this.instance = Objects.requireNonNull(instance, "instance");
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.meta = Objects.requireNonNull(meta, "meta");
        this.sidebar = Objects.requireNonNull(sidebar, "sidebar");
    }

    @Override
    public void start() {
        playerView.getPlayer().ifPresent(player -> {
            sidebar.removeViewer(player);
            player.getInventory().clear();
        });
        playerView.getDisplayName().thenAccept(displayName -> {
            instance.sendMessage(displayName.append(Component.text(" quit.")));
        });
        meta.setInGame(false);
        meta.setCanRevive(false);
        meta.setCanTriggerSLA(false);
    }

    @Override
    public void end() {
        meta.setInGame(true);
    }
}
