package org.phantazm.core.scene2.lobby;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.scene2.CreatingJoin;
import org.phantazm.core.scene2.SceneCreator;
import org.phantazm.core.scene2.TablistSettingJoin;

import java.util.Collection;

public class JoinLobby extends CreatingJoin<Lobby> implements TablistSettingJoin<Lobby> {
    public JoinLobby(@NotNull Collection<@NotNull PlayerView> players, @NotNull SceneCreator<Lobby> sceneCreator) {
        super(players, Lobby.class, sceneCreator);
    }

    @Override
    public void join(@NotNull Lobby scene) {
        TablistSettingJoin.super.join(scene);
        scene.join(players());
    }
}
