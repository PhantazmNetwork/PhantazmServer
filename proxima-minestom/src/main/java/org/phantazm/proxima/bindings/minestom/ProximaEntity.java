package org.phantazm.proxima.bindings.minestom;

import com.github.steanky.proxima.path.PathSettings;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.UUID;

public class ProximaEntity extends LivingEntity {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ProximaEntity.class);

    private final Pathfinding pathfinding;

    public ProximaEntity(@NotNull EntityType entityType, @NotNull UUID uuid, @NotNull Pathfinding pathfinding) {
        super(entityType, uuid);
        this.pathfinding = Objects.requireNonNull(pathfinding, "pathfinding");
    }

    public @NotNull Pathfinding pathfinding() {
        return pathfinding;
    }
}
