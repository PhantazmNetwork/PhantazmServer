package org.phantazm.proxima.bindings.minestom;

import com.github.steanky.vector.Bounds3I;
import com.github.steanky.vector.HashVec3I2ObjectMap;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Function;

public class InstanceSettingsFunction implements Function<Instance, InstanceSpawner.InstanceSettings> {
    private final EventNode<Event> rootNode;
    private final Map<Instance, InstanceSpawner.InstanceSettings> settingsMap;

    public InstanceSettingsFunction(@NotNull EventNode<Event> rootNode) {
        this.rootNode = Objects.requireNonNull(rootNode, "rootNode");
        this.settingsMap = new WeakHashMap<>();
    }

    @Override
    public @Nullable InstanceSpawner.InstanceSettings apply(@NotNull Instance instance) {
        return settingsMap.computeIfAbsent(instance, key -> {
            DimensionType dimensionType = key.getDimensionType();

            int height = dimensionType.getHeight(); //range (0, 4064)
            int minY = dimensionType.getMinY(); //range (-2032, 2031)

            WorldBorder border = key.getWorldBorder();
            int centerX = (int)Math.floor(border.getCenterX());
            int centerZ = (int)Math.floor(border.getCenterZ());

            int diameter = Math.min((int)Math.floor(border.getDiameter() / 2), 60000000);
            int halfDiameter = diameter / 2;

            Bounds3I bounds = Bounds3I.immutable(centerX - halfDiameter, minY, centerZ - halfDiameter, diameter, height,
                    diameter);

            return new InstanceSpawner.InstanceSettings(
                    ThreadLocal.withInitial(() -> new HashVec3I2ObjectMap<>(bounds)),
                    new BasicInstanceSpaceHandler(key, rootNode));
        });
    }
}
