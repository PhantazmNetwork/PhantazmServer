package org.phantazm.core.scene2.lobby;

import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.scene2.InstanceScene;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Lobby extends InstanceScene {
    private final Set<PlayerView> players;
    private final Set<PlayerView> playersView;

    public Lobby(@NotNull Instance instance) {
        super(instance);
        this.players = new HashSet<>();
        this.playersView = Collections.unmodifiableSet(players);
    }

    @Override
    public @NotNull @UnmodifiableView Set<PlayerView> players() {
        return playersView;
    }

    @Override
    public boolean preventsServerShutdown() {
        return false;
    }

    @Override
    public void leave(@NotNull Set<? extends @NotNull PlayerView> players) {
        this.players.removeAll(players);
    }

    @Override
    public void tick(long time) {

    }
}
