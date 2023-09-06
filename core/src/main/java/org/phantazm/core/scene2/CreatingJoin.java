package org.phantazm.core.scene2;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.scene2.lobby.Lobby;

import java.util.Collection;
import java.util.Objects;

public abstract class CreatingJoin<T extends Scene> extends JoinAbstract<T> {
    private final SceneCreator<T> sceneCreator;

    public CreatingJoin(@NotNull Collection<@NotNull PlayerView> players, @NotNull Class<T> targetType,
        @NotNull SceneCreator<T> sceneCreator) {
        super(players, targetType);
        this.sceneCreator = Objects.requireNonNull(sceneCreator);
    }

    @Override
    public @NotNull T createNewScene(@NotNull SceneManager manager) {
        return sceneCreator.createScene();
    }

    @Override
    public boolean canCreateNewScene(@NotNull SceneManager manager) {
        int sceneCap = sceneCreator.sceneCap();
        if (sceneCap < 0) {
            return true;
        }

        int currentAmount = manager.amount(Lobby.class);
        if (currentAmount == -1) {
            return false;
        }

        return currentAmount + 1 <= sceneCap;
    }

    @Override
    public boolean matches(@NotNull T scene) {
        int playerCap = sceneCreator.playerCap();
        if (playerCap < 0) {
            return true;
        }

        return scene.players().size() + super.players().size() <= playerCap;
    }
}
