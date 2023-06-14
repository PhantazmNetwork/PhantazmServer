package org.phantazm.zombies.scene;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.core.game.scene.SceneJoinRequest;

import java.util.Objects;
import java.util.UUID;

public record ZombiesRouteRequest(@Nullable Key targetMap,
                                  @Nullable UUID targetGame,
                                  @NotNull ZombiesJoinRequest joinRequest) implements SceneJoinRequest {

    public ZombiesRouteRequest {
        Objects.requireNonNull(joinRequest, "joinRequest");
    }

    public static ZombiesRouteRequest joinGame(@NotNull Key targetMap, @NotNull ZombiesJoinRequest joinRequest) {
        return new ZombiesRouteRequest(targetMap, null, joinRequest);
    }

    public static ZombiesRouteRequest rejoinGame(@NotNull UUID targetGame, @NotNull ZombiesJoinRequest joinRequest) {
        return new ZombiesRouteRequest(null, targetGame, joinRequest);
    }

    @Override
    public int getRequestWeight() {
        return joinRequest.getRequestWeight();
    }
}
