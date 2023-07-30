package org.phantazm.proxima.bindings.minestom;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface Spawner {
    @NotNull ProximaEntity spawn(@NotNull Instance instance, @NotNull Pos pos, @NotNull EntityType entityType,
            @NotNull Pathfinding.Factory factory, @NotNull Consumer<? super ProximaEntity> init);

    default @NotNull ProximaEntity spawn(@NotNull Instance instance, @NotNull Pos pos, @NotNull EntityType entityType,
            @NotNull Pathfinding.Factory factory) {
        return spawn(instance, pos, entityType, factory, ignored -> {
        });
    }
}
