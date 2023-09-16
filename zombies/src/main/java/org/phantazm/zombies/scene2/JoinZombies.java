package org.phantazm.zombies.scene2;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.scene2.CreatingJoin;
import org.phantazm.core.scene2.SceneCreator;
import org.phantazm.core.scene2.TablistLocalJoin;
import org.phantazm.zombies.stage.Stage;

import java.util.Collection;

public class JoinZombies extends CreatingJoin<ZombiesScene> implements TablistLocalJoin<ZombiesScene> {
    public JoinZombies(@NotNull Collection<@NotNull PlayerView> players,
        @NotNull SceneCreator<ZombiesScene> sceneCreator) {
        super(players, ZombiesScene.class, sceneCreator);
    }

    @Override
    public void join(@NotNull ZombiesScene scene) {
        TablistLocalJoin.super.join(scene);
        scene.join(playerViews());
    }

    @Override
    public boolean matches(@NotNull ZombiesScene scene) {
        Stage stage = scene.currentStage();
        return super.matches(scene) && (stage != null && stage.canJoin());
    }
}
