package org.phantazm.core.scene2;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;

import java.util.Collection;
import java.util.Objects;

public abstract class CreatingJoin<T extends Scene> extends JoinAbstract<T> {
    protected final SceneCreator<T> sceneCreator;

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
        int playerCap = sceneCreator.playerCap();

        int currentAmount = manager.amount(targetType());
        if (currentAmount < 0) {
            return false;
        }

        return (sceneCap < 0 || currentAmount + 1 <= sceneCap) && (playerCap < 0 || playerViews().size() <= playerCap)
            && sceneCreator.canCreateOrJoin(playerViews());
    }

    @Override
    public boolean matches(@NotNull T scene) {
        int playerCap = sceneCreator.playerCap();
        return playerCap < 0 || scene.playerCount() + newPlayerCount(scene) <= playerCap &&
            sceneCreator.canCreateOrJoin(playerViews());
    }

    /**
     * Determines the number of new players that will be joining this scene by only counting join participants which are
     * not already in the scene.
     *
     * @param scene the scene to join
     * @return the number of players that aren't already in the scene
     */
    public int newPlayerCount(@NotNull T scene) {
        int newPlayers = 0;
        for (PlayerView joining : playerViews()) {
            if (!scene.hasPlayer(joining)) {
                newPlayers++;
            }
        }

        return newPlayers;
    }
}
