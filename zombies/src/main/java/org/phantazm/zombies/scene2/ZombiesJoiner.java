package org.phantazm.zombies.scene2;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.scene2.Join;
import org.phantazm.core.scene2.SceneCreator;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ZombiesJoiner {
    private final Map<Key, SceneCreator<ZombiesScene>> sceneCreatorMap;

    public ZombiesJoiner(@NotNull Map<Key, SceneCreator<ZombiesScene>> sceneCreatorMap) {
        this.sceneCreatorMap = Map.copyOf(sceneCreatorMap);
    }

    private SceneCreator<ZombiesScene> validateKey(Key key) {
        Objects.requireNonNull(key);
        SceneCreator<ZombiesScene> creator = sceneCreatorMap.get(key);
        if (creator == null) {
            throw new IllegalArgumentException("map " + key + " does not exist");
        }

        return creator;
    }

    public @NotNull Join<ZombiesScene> joinMap(@NotNull Set<@NotNull PlayerView> players, @NotNull Key mapKey) {
        return joinMap(players, mapKey, Set.of());
    }

    public @NotNull Join<ZombiesScene> joinMap(@NotNull Set<@NotNull PlayerView> players, @NotNull Key mapKey,
        @NotNull Set<@NotNull Key> modifiers) {
        return new JoinZombiesMap(players, validateKey(mapKey), mapKey, modifiers);
    }

    public @NotNull Join<ZombiesScene> rejoin(@NotNull Set<@NotNull PlayerView> players, @Nullable UUID sceneId) {
        return new RejoinZombies(players, sceneId);
    }

    public @NotNull Join<ZombiesScene> rejoin(@NotNull Set<@NotNull PlayerView> players) {
        return rejoin(players, null);
    }

    public @NotNull Join<ZombiesScene> joinRestricted(@NotNull Set<@NotNull PlayerView> players, @NotNull Key mapKey) {
        return joinRestricted(players, mapKey, Set.of());
    }

    public @NotNull Join<ZombiesScene> joinRestricted(@NotNull Set<@NotNull PlayerView> players, @NotNull Key mapKey,
        @NotNull Set<@NotNull Key> modifiers) {
        return new JoinZombiesRestricted(players, validateKey(mapKey), mapKey, modifiers);
    }

    public @NotNull Join<ZombiesScene> joinSandbox(@NotNull Set<@NotNull PlayerView> players, @NotNull Key mapKey) {
        return joinSandbox(players, mapKey, Set.of());
    }

    public @NotNull Join<ZombiesScene> joinSandbox(@NotNull Set<@NotNull PlayerView> players, @NotNull Key mapKey,
        @NotNull Set<@NotNull Key> modifiers) {
        return new JoinZombiesRestricted(players, validateKey(mapKey), mapKey, modifiers, true);
    }
}
