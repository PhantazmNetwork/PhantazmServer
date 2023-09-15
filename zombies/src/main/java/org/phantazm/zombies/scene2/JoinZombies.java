package org.phantazm.zombies.scene2;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.scene2.CreatingJoin;
import org.phantazm.core.scene2.SceneCreator;

import java.util.Collection;

public class JoinZombies extends CreatingJoin<ZombiesScene> {
    public JoinZombies(@NotNull Collection<@NotNull PlayerView> players, @NotNull Class<ZombiesScene> targetType,
        @NotNull SceneCreator<ZombiesScene> sceneCreator) {
        super(players, targetType, sceneCreator);
    }

    @Override
    public void join(@NotNull ZombiesScene scene) {

    }
}
