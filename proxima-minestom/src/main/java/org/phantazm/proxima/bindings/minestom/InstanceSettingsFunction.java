package org.phantazm.proxima.bindings.minestom;

import com.github.steanky.proxima.node.Node;
import com.github.steanky.vector.Bounds3I;
import com.github.steanky.vector.HashVec3I2ObjectMap;
import com.github.steanky.vector.Vec3I2ObjectMap;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.InstanceUnregisterEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class InstanceSettingsFunction implements Function<Instance, InstanceSpawner.InstanceSettings> {
    private final EventNode<Event> rootNode;
    private final Map<UUID, InstanceSpawner.InstanceSettings> settingsMap;

    public InstanceSettingsFunction(@NotNull EventNode<Event> rootNode) {
        this.rootNode = rootNode;
        this.settingsMap = new ConcurrentHashMap<>();
        rootNode.addListener(InstanceUnregisterEvent.class, this::onInstanceUnregister);
    }

    @Override
    public @NotNull InstanceSpawner.InstanceSettings apply(@NotNull Instance instance) {
        return settingsMap.computeIfAbsent(instance.getUniqueId(), ignored -> {
            if (!instance.isRegistered()) {
                throw new IllegalArgumentException("Cannot get settings for unregistered instance");
            }

            DimensionType dimensionType = instance.getDimensionType();

            int height = dimensionType.getHeight(); //range (0, 4064)
            int minY = dimensionType.getMinY(); //range (-2032, 2031)

            WorldBorder border = instance.getWorldBorder();
            int centerX = (int)Math.floor(border.getCenterX());
            int centerZ = (int)Math.floor(border.getCenterZ());

            int diameter = Math.min((int)Math.floor(border.getDiameter() / 2), 60000000);
            int halfDiameter = diameter / 2;

            Bounds3I bounds = Bounds3I.immutable(centerX - halfDiameter, minY, centerZ - halfDiameter, diameter, height,
                    diameter);

            InstanceSpace instanceSpace = new InstanceSpace(instance);
            ThreadLocal<Vec3I2ObjectMap<Node>> local = ThreadLocal.withInitial(() -> new HashVec3I2ObjectMap<>(bounds));

            EventNode<InstanceEvent> node =
                    EventNode.type("pathfinding_node_" + instance.getUniqueId(), EventFilter.INSTANCE);
            rootNode.addChild(node);

            BasicInstanceSpaceHandler instanceSpaceHandler = new BasicInstanceSpaceHandler(instanceSpace, node);
            return new InstanceSpawner.InstanceSettings(local, node, instanceSpaceHandler);
        });
    }

    private void onInstanceUnregister(InstanceUnregisterEvent event) {
        InstanceSpawner.InstanceSettings settings = settingsMap.remove(event.getInstance().getUniqueId());

        if (settings != null) {
            rootNode.removeChild(settings.instanceNode());
            settings.spaceHandler().space().clearCache();
        }
    }
}
