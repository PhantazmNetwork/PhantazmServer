package org.phantazm.core.scene2.event;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.scene2.SceneManager;

import java.util.Objects;
import java.util.Set;

public record SceneJoinEvent(SceneManager.JoinResult<?> result,
    Set<PlayerView> players) implements Event {
    public SceneJoinEvent(@NotNull SceneManager.JoinResult<?> result, @NotNull Set<@NotNull PlayerView> players) {
        this.result = Objects.requireNonNull(result);
        this.players = Objects.requireNonNull(players);
    }
}
