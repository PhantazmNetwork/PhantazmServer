package org.phantazm.core.scene2.event;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.scene2.Scene;

import java.util.Objects;
import java.util.Set;

public record SceneJoinEvent(Scene scene,
    Set<PlayerView> players) implements SceneEvent {
    public SceneJoinEvent(@NotNull Scene scene, @NotNull Set<@NotNull PlayerView> players) {
        this.scene = Objects.requireNonNull(scene);
        this.players = Objects.requireNonNull(players);
    }
}
