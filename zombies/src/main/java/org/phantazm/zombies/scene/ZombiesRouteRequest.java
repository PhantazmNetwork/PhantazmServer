package org.phantazm.zombies.scene;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.SceneJoinRequest;

public record ZombiesRouteRequest(@NotNull Key targetMap, @NotNull ZombiesJoinRequest joinRequest)
        implements SceneJoinRequest {

    @Override
    public int getRequestWeight() {
        return joinRequest.getRequestWeight();
    }
}
