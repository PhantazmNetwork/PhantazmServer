package org.phantazm.core.scene2.join;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.scene2.*;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public class JoinSpectator<T extends WatchableScene & IdentifiableScene> extends JoinAbstract<T> implements Join<T>,
    TablistLocalJoin<T> {
    private final boolean ghost;
    private final UUID targetUuid;

    public JoinSpectator(@NotNull Collection<@NotNull PlayerView> players,
        @NotNull Class<? extends T> targetType, boolean ghost, @NotNull UUID targetUuid) {
        super(players, targetType);
        this.ghost = ghost;
        this.targetUuid = Objects.requireNonNull(targetUuid);
    }

    @Override
    public @NotNull T createNewScene(@NotNull SceneManager manager) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canCreateNewScene(@NotNull SceneManager manager) {
        return false;
    }

    @Override
    public void join(@NotNull T scene) {
        TablistLocalJoin.super.join(scene);
    }

    @Override
    public void postJoin(@NotNull T scene) {
        scene.joinSpectators(playerViews(), ghost);
    }

    @Override
    public boolean matches(@NotNull T scene) {
        return scene.isGame() && scene.identity().equals(targetUuid);
    }

    @Override
    public @NotNull ViewResult visibility(@NotNull WatchableScene scene, @NotNull PlayerView first,
        @NotNull PlayerView second, @NotNull Type type) {
        return switch (type) {
            case BOTH_JOINING -> ViewResult.BOTH_SEE; //both are spectators
            case FIRST_JOINING -> {
                if (scene.hasSpectator(second)) {
                    yield ViewResult.BOTH_SEE;
                }

                yield ViewResult.FIRST_SEES_SECOND;
            }
            case SECOND_JOINING -> {
                if (scene.hasSpectator(first)) {
                    yield ViewResult.BOTH_SEE;
                }

                yield ViewResult.SECOND_SEES_FIRST;
            }
        };
    }
}
