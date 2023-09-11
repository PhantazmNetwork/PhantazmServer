package org.phantazm.core.scene2.lobby;

import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerTablistShowEvent;
import net.minestom.server.thread.Acquired;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.scene2.CreatingJoin;
import org.phantazm.core.scene2.Scene;
import org.phantazm.core.scene2.SceneCreator;
import org.phantazm.core.scene2.SceneManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JoinLobby extends CreatingJoin<Lobby> implements SceneManager.LoginJoin<Lobby> {
    private final boolean login;

    public JoinLobby(@NotNull Collection<@NotNull PlayerView> players, @NotNull SceneCreator<Lobby> sceneCreator,
        boolean login) {
        super(players, Lobby.class, sceneCreator);
        this.login = login;
    }

    public JoinLobby(@NotNull Collection<@NotNull PlayerView> players, @NotNull SceneCreator<Lobby> sceneCreator) {
        this(players, sceneCreator, false);
    }

    @Override
    public void join(@NotNull Lobby scene) {
        scene.join(players(), login);
    }

    @Override
    public void postSpawn(@NotNull Lobby scene) {
        scene.getAcquirable().sync(self -> ((Lobby) self).postLogin(players()));
    }

    @Override
    public void updateTablist(@NotNull Lobby scene, @NotNull PlayerTablistShowEvent event) {
        Acquired<? extends Scene> acquired = scene.getAcquirable().lock();
        List<Player> participants;
        try {
            Scene self = acquired.get();
            participants = new ArrayList<>(self.playerCount());

            for (PlayerView playerView : self.playersView()) {
                playerView.getPlayer().ifPresent(participants::add);
            }
        } finally {
            acquired.unlock();
        }

        event.setTablistParticipants(participants);
    }
}
