package org.phantazm.zombies.scene2;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.scene2.SceneCreator;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.loader.Loader;
import org.phantazm.zombies.modifier.ModifierHandler;

import java.util.Collection;
import java.util.Set;

public class JoinZombiesRestricted extends JoinZombiesMap {
    private final boolean sandbox;

    public JoinZombiesRestricted(@NotNull Collection<@NotNull PlayerView> players,
        @NotNull SceneCreator<ZombiesScene> sceneCreator, @NotNull Key mapKey,
        @NotNull Set<Key> modifiers, boolean sandbox, @NotNull Loader<ModifierHandler> modifierHandlerLoader) {
        super(players, sceneCreator, mapKey, modifiers, modifierHandlerLoader);
        this.sandbox = sandbox;
    }

    public JoinZombiesRestricted(@NotNull Collection<@NotNull PlayerView> players,
        @NotNull SceneCreator<ZombiesScene> sceneCreator, @NotNull Key mapKey,
        @NotNull Set<Key> modifiers, @NotNull Loader<ModifierHandler> modifierHandlerLoader) {
        this(players, sceneCreator, mapKey, modifiers, false, modifierHandlerLoader);
    }

    @Override
    public @NotNull ZombiesScene createNewScene(@NotNull SceneManager manager) {
        ZombiesScene newScene = super.createNewScene(manager);
        newScene.setRestricted(true);
        newScene.setSandbox(sandbox);

        return newScene;
    }

    @Override
    public boolean matches(@NotNull ZombiesScene scene) {
        return false;
    }
}
