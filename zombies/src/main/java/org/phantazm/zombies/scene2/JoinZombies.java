package org.phantazm.zombies.scene2;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.scene2.CreatingJoin;
import org.phantazm.core.scene2.SceneCreator;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.core.scene2.TablistLocalJoin;
import org.phantazm.loader.Loader;
import org.phantazm.zombies.modifier.ModifierHandler;
import org.phantazm.zombies.stage.Stage;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class JoinZombies extends CreatingJoin<ZombiesScene> implements TablistLocalJoin<ZombiesScene> {
    private final Set<Key> modifiers;
    private final Loader<ModifierHandler> modifierHandlerLoader;

    public JoinZombies(@NotNull Collection<@NotNull PlayerView> players,
        @NotNull SceneCreator<ZombiesScene> sceneCreator,
        @NotNull Set<Key> modifiers,
        @NotNull Loader<ModifierHandler> modifierHandlerLoader) {
        super(players, ZombiesScene.class, sceneCreator);
        this.modifiers = Objects.requireNonNull(modifiers);
        this.modifierHandlerLoader = Objects.requireNonNull(modifierHandlerLoader);
    }

    @Override
    public void join(@NotNull ZombiesScene scene) {
        TablistLocalJoin.super.join(scene);
        scene.join(playerViews());
    }

    @Override
    public @NotNull ZombiesScene createNewScene(@NotNull SceneManager manager) {
        ZombiesScene scene = super.createNewScene(manager);
        modifierHandlerLoader.first().applyModifiers(modifiers, scene);
        return scene;
    }

    @Override
    public boolean matches(@NotNull ZombiesScene scene) {
        Stage stage = scene.currentStage();
        return super.matches(scene) && (stage != null && stage.canJoin()) &&
            modifiers.equals(scene.activeModifiers().stream().map(Keyed::key).collect(Collectors.toUnmodifiableSet()));
    }
}
