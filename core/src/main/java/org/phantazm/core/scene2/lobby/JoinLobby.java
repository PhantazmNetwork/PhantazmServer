package org.phantazm.core.scene2.lobby;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.scene2.CreatingJoin;
import org.phantazm.core.scene2.SceneCreator;
import org.phantazm.core.scene2.SceneManager;

import java.util.Collection;
import java.util.List;

public class JoinLobby extends CreatingJoin<Lobby> implements SceneManager.LoginJoin<Lobby> {
    private final boolean login;

    private Lobby scene;

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
        if (login) {
            this.scene = scene;
        }

        scene.join(players(), login);
    }

    @Override
    public void postSpawn() {
        Lobby scene = this.scene;
        if (scene == null || !login) {
            return;
        }

        scene.getAcquirable().sync(self -> ((Lobby) self).postLogin(players()));
        this.scene = null;
    }

    @Override
    public void updateTablist(@NotNull List<@NotNull Player> tablistRecipients) {
        Lobby scene = this.scene;
        if (scene == null || !login) {
            return;
        }

        for (PlayerView playerView : players()) {
            playerView.getPlayer().ifPresent(tablistRecipients::add);
        }

        scene.getAcquirable().sync(self -> {
            for (PlayerView playerView : self.playersView()) {
                playerView.getPlayer().ifPresent(tablistRecipients::add);
            }
        });
    }
}
