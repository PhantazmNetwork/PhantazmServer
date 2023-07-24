package org.phantazm.proxima.bindings.minestom;

import com.github.steanky.proxima.node.Node;
import com.github.steanky.proxima.path.Pathfinder;
import com.github.steanky.vector.Vec3I2ObjectMap;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class InstanceSpawner implements Spawner {
    public record InstanceSettings(@NotNull ThreadLocal<Vec3I2ObjectMap<Node>> nodeLocal,
                                   @NotNull EventNode<InstanceEvent> instanceNode,
                                   @NotNull InstanceSpaceHandler spaceHandler) {

    }

    private final Pathfinder pathfinder;
    private final Function<? super Instance, ? extends InstanceSettings> settingsFunction;

    public InstanceSpawner(@NotNull Pathfinder pathfinder,
            @NotNull Function<? super Instance, ? extends InstanceSettings> settingsFunction) {
        this.pathfinder = Objects.requireNonNull(pathfinder, "pathfinder");
        this.settingsFunction = Objects.requireNonNull(settingsFunction, "settingsFunction");
    }

    @Override
    public @NotNull ProximaEntity spawn(@NotNull Instance instance, @NotNull Pos pos, @NotNull EntityType entityType,
            @NotNull Pathfinding.Factory factory, @NotNull Consumer<? super ProximaEntity> init) {
        InstanceSettings settings = settingsFunction.apply(instance);
        if (settings == null) {
            throw new IllegalStateException(
                    "Unable to spawn entity in instance " + instance.getUniqueId() + ", " + "missing InstanceSettings");
        }

        Pathfinding pathfinding = factory.make(pathfinder, settings.nodeLocal, settings.spaceHandler);
        ProximaEntity entity = new ProximaEntity(entityType, UUID.randomUUID(), pathfinding);
        init.accept(entity);

        entity.setInstance(instance, pos);
        return entity;
    }
}
