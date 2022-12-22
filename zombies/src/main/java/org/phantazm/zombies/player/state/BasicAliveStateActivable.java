package org.phantazm.zombies.player.state;

import net.minestom.server.entity.GameMode;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Activable;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.player.ZombiesPlayerMeta;

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
