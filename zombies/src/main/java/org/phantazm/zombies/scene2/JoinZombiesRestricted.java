package org.phantazm.zombies.scene2;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.scene2.SceneCreator;
import org.phantazm.core.scene2.SceneManager;

import java.util.Collection;

public class JoinZombiesRestricted extends JoinZombiesMap {
    public JoinZombiesRestricted(@NotNull Collection<@NotNull PlayerView> players,
        @NotNull SceneCreator<ZombiesScene> sceneCreator, @NotNull Key mapKey) {
        super(players, sceneCreator, mapKey);
    }

    @Override
    public @NotNull ZombiesScene createNewScene(@NotNull SceneManager manager) {
        ZombiesScene newScene = super.createNewScene(manager);
        newScene.setJoinable(false);

        return newScene;
    }

    @Override
    public boolean matches(@NotNull ZombiesScene scene) {
        return false;
    }
}
