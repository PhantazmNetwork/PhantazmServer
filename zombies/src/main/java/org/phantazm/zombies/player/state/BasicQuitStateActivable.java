package org.phantazm.zombies.player.state;

import net.kyori.adventure.text.Component;
import net.minestom.server.instance.Instance;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Activable;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.player.ZombiesPlayerMeta;

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
        playerView.getDisplayName()
                .thenAccept(displayName -> instance.sendMessage(displayName.append(Component.text(" quit."))));
        meta.setInGame(false);
        meta.setCanRevive(false);
        meta.setCanTriggerSLA(false);
    }

    @Override
    public void end() {
        meta.setInGame(true);
    }
}
