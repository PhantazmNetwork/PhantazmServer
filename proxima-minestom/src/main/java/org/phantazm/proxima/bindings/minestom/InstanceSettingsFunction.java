package org.phantazm.proxima.bindings.minestom;

import com.github.steanky.proxima.node.Node;
import com.github.steanky.vector.Bounds3I;
import com.github.steanky.vector.HashVec3I2ObjectMap;
import com.github.steanky.vector.Vec3I2ObjectMap;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;

public class InstanceSettingsFunction implements Function<Instance, InstanceSpawner.InstanceSettings> {
    private static final int MAX_READ_TRIES = 10;

    private final EventNode<Event> rootNode;

    private final StampedLock stampedLock;
    private final Map<Instance, InstanceSpawner.InstanceSettings> settingsMap;

    public InstanceSettingsFunction(@NotNull EventNode<Event> rootNode) {
        this.rootNode = Objects.requireNonNull(rootNode, "rootNode");
        this.stampedLock = new StampedLock();
        this.settingsMap = new WeakHashMap<>();
    }

    @Override
    public @NotNull InstanceSpawner.InstanceSettings apply(@NotNull Instance instance) {
        InstanceSpawner.InstanceSettings settings = null;

        boolean lockBroken = true;
        int tries = 0;
        do {
            long optimisticStamp = stampedLock.tryOptimisticRead();
            try {
                settings = settingsMap.get(instance);
            }
            catch (Throwable ignored) {
            }

            if (stampedLock.validate(optimisticStamp)) {
                lockBroken = false;
                break;
            }
        } while (++tries <= MAX_READ_TRIES);

        if (lockBroken) {
            long fullReadLock = stampedLock.readLock();
            try {
                settings = settingsMap.get(instance);
            }
            finally {
                stampedLock.unlockRead(fullReadLock);
            }
        }

        if (settings != null) {
            return settings;
        }

        long writeLock = stampedLock.writeLock();
        try {
            settings = settingsMap.get(instance);
            if (settings != null) {
                return settings;
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
            BasicInstanceSpaceHandler instanceSpaceHandler = new BasicInstanceSpaceHandler(instanceSpace, rootNode);
            instanceSpaceHandler.registerEvents();

            settings = new InstanceSpawner.InstanceSettings(local, instanceSpaceHandler);
            settingsMap.put(instance, settings);

            return settings;
        }
        finally {
            stampedLock.unlockWrite(writeLock);
        }
    }
}
