package com.github.phantazmnetwork.zombies.game.scoreboard.score;

import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.zombies.game.kill.PlayerKills;
import net.minestom.server.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ZombieKillsScoreboardUpdater implements ScoreboardUpdater {

    private final Scoreboard scoreboard;

    private final PlayerView playerView;

    private final PlayerKills kills;

    private int killCount = -1;

    public ZombieKillsScoreboardUpdater(@NotNull Scoreboard scoreboard, @NotNull PlayerView playerView,
                                        @NotNull PlayerKills kills) {
        this.scoreboard = Objects.requireNonNull(scoreboard, "scoreboard");
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.kills = Objects.requireNonNull(kills, "kills");
    }

    @Override
    public void invalidateCache() {
        killCount = -1;
    }

    @Override
    public void tick(long time) {
        if (killCount == -1 || killCount != kills.getKills()) {
            killCount = kills.getKills();
            playerView.getPlayer().ifPresent(player -> scoreboard.updateScore(player, killCount));
        }
    }
}
