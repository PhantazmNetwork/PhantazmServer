package org.phantazm.core.scene2.lobby;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.scene2.CreatingJoin;
import org.phantazm.core.scene2.SceneCreator;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.core.scene2.TablistSettingJoin;

import java.util.Collection;

public class JoinLobby extends CreatingJoin<Lobby> implements TablistSettingJoin<Lobby>, SceneManager.LoginJoin<Lobby> {
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

        TablistSettingJoin.super.join(scene);
        scene.join(players(), login);
    }

    @Override
    public void postSpawn() {
        if (scene == null || !login) {
            return;
        }

        scene.getAcquirable().sync(self -> {
            ((Lobby) self).postLogin(players());
        });
        scene = null;
    }
}
