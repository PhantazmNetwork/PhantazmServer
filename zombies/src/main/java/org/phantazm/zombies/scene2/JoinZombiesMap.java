package org.phantazm.zombies.scene2;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.scene2.SceneCreator;
import org.phantazm.loader.Loader;
import org.phantazm.zombies.modifier.ModifierHandler;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public class JoinZombiesMap extends JoinZombies {
    private final Key mapKey;

    public JoinZombiesMap(@NotNull Collection<@NotNull PlayerView> players,
        @NotNull SceneCreator<ZombiesScene> sceneCreator, @NotNull Key mapKey,
        @NotNull Set<Key> modifiers, @NotNull Loader<ModifierHandler> modifierHandlerLoader) {
        super(players, sceneCreator, modifiers, modifierHandlerLoader);
        this.mapKey = Objects.requireNonNull(mapKey);
    }

    @Override
    public boolean matches(@NotNull ZombiesScene scene) {
        return super.matches(scene) && scene.mapSettingsInfo().id().equals(mapKey);
    }
}
