package org.phantazm.zombies.scene2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.scene2.JoinAbstract;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.core.scene2.TablistLocalJoin;
import org.phantazm.zombies.stage.Stage;

import java.util.Collection;
import java.util.UUID;

public class RejoinZombies extends JoinAbstract<ZombiesScene> implements TablistLocalJoin<ZombiesScene> {
    private final UUID uuid;

    public RejoinZombies(@NotNull Collection<@NotNull PlayerView> players, @Nullable UUID uuid) {
        super(players, ZombiesScene.class);
        this.uuid = uuid;
    }

    @Override
    public @NotNull ZombiesScene createNewScene(@NotNull SceneManager manager) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canCreateNewScene(@NotNull SceneManager manager) {
        return false;
    }

    @Override
    public void join(@NotNull ZombiesScene scene) {
        scene.join(playerViews());
    }

    @Override
    public boolean matches(@NotNull ZombiesScene scene) {
        Stage stage = scene.currentStage();
        return (uuid == null || scene.identity().equals(uuid)) &&
            (stage != null && stage.canRejoin()) && scene.managedPlayers().keySet().containsAll(playerViews());
    }
}
