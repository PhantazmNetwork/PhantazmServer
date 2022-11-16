package com.github.phantazmnetwork.zombies.player.state;

import com.github.phantazmnetwork.commons.Activable;
import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayerMeta;
import net.minestom.server.entity.GameMode;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BasicAliveStateActivable implements Activable {

    private final PlayerView playerView;

    private final ZombiesPlayerMeta meta;

    private final Sidebar sidebar;

    public BasicAliveStateActivable(@NotNull PlayerView playerView, @NotNull ZombiesPlayerMeta meta,
            @NotNull Sidebar sidebar) {
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.meta = Objects.requireNonNull(meta, "meta");
        this.sidebar = Objects.requireNonNull(sidebar, "sidebar");
    }

    @Override
    public void start() {
        playerView.getPlayer().ifPresent(player -> {
            player.setInvisible(false);
            player.setAllowFlying(false);
            player.setFlying(false);
            player.setGameMode(GameMode.ADVENTURE);
            sidebar.addViewer(player);
        });
        meta.setInGame(true);
        meta.setCanRevive(true);
        meta.setCanTriggerSLA(true);
    }

    @Override
    public void end() {
        playerView.getPlayer().ifPresent(player -> {
            player.setInvisible(false);
            player.setAllowFlying(false);
            player.setFlying(false);
            player.setGameMode(GameMode.ADVENTURE);
            sidebar.addViewer(player);
        });
    }
}
