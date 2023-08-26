package org.phantazm.core.guild;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;

import java.util.Objects;

public class GuildMember {

    private final PlayerView playerView;

    public GuildMember(@NotNull PlayerView playerView) {
        this.playerView = Objects.requireNonNull(playerView);
    }

    public @NotNull PlayerView getPlayerView() {
        return playerView;
    }

    public boolean isOnline() {
        return playerView.getPlayer().isPresent();
    }

}
