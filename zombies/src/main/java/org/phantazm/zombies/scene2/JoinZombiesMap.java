package org.phantazm.zombies.scene2;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.scene2.SceneCreator;

import java.util.Collection;

public class JoinZombiesMap extends JoinZombies {
    private final Key mapKey;

    public JoinZombiesMap(@NotNull Collection<@NotNull PlayerView> players,
        @NotNull SceneCreator<ZombiesScene> sceneCreator, @NotNull Key mapKey) {
        super(players, sceneCreator);
        this.mapKey = mapKey;
    }

    @Override
    public boolean matches(@NotNull ZombiesScene scene) {
        return super.matches(scene) && scene.mapSettingsInfo().id().equals(mapKey);
    }
}
