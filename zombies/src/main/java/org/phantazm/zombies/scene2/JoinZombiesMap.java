package org.phantazm.zombies.scene2;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.scene2.SceneCreator;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.zombies.modifier.ModifierHandler;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public class JoinZombiesMap extends JoinZombies {
    private final Key mapKey;
    private final ModifierHandler modifierHandler;
    private final Set<Key> modifiers;

    public JoinZombiesMap(@NotNull Collection<@NotNull PlayerView> players,
        @NotNull SceneCreator<ZombiesScene> sceneCreator, @NotNull Key mapKey,
        @NotNull ModifierHandler modifierHandler, @NotNull Set<Key> modifiers) {
        super(players, sceneCreator);
        this.mapKey = Objects.requireNonNull(mapKey);
        this.modifierHandler = Objects.requireNonNull(modifierHandler);
        this.modifiers = Objects.requireNonNull(modifiers);
    }

    @Override
    public @NotNull ZombiesScene createNewScene(@NotNull SceneManager manager) {
        ZombiesScene scene = super.createNewScene(manager);
        modifierHandler.applyModifiers(modifiers, scene);

        return scene;
    }

    @Override
    public boolean matches(@NotNull ZombiesScene scene) {
        return super.matches(scene) && scene.mapSettingsInfo().id().equals(mapKey);
    }
}
