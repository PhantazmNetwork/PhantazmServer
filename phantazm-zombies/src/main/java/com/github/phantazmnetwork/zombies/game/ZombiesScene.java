package com.github.phantazmnetwork.zombies.game;

import com.github.phantazmnetwork.api.game.scene.InstanceScene;
import com.github.phantazmnetwork.api.game.scene.RouteResult;
import com.github.phantazmnetwork.api.game.scene.fallback.SceneFallback;
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class ZombiesScene extends InstanceScene<ZombiesJoinRequest> {
    private GameState state;
    private final ZombiesMap map;

    public ZombiesScene(@NotNull Instance instance, @NotNull SceneFallback fallback, @NotNull ZombiesMap map) {
        super(instance, fallback);
        this.map = Objects.requireNonNull(map, "map");

        state = GameState.IDLE;
    }

    @Override
    public @NotNull RouteResult join(@NotNull ZombiesJoinRequest joinRequest) {
        return null;
    }

    @Override
    public @NotNull RouteResult leave(@NotNull Iterable<UUID> leavers) {
        return null;
    }

    @Override
    public boolean isJoinable() {
        return false;
    }

    @Override
    public void setJoinable(boolean joinable) {

    }

    public @NotNull GameState getState() {
        return state;
    }
}
