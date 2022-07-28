package com.github.phantazmnetwork.zombies.game;

import com.github.phantazmnetwork.core.game.scene.SceneJoinRequest;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public record ZombiesRouteRequest(@NotNull Key targetMap, @NotNull ZombiesJoinRequest joinRequest)
        implements SceneJoinRequest {

    @Override
    public int getRequestWeight() {
        return joinRequest.getRequestWeight();
    }
}
