package org.phantazm.commons.vector;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import com.github.steanky.vector.Bounds3D;
import com.github.steanky.vector.Bounds3I;
import com.github.steanky.vector.Vec3D;
import com.github.steanky.vector.Vec3I;
import org.jetbrains.annotations.NotNull;

/**
 * Contains static {@link ConfigProcessor} implementations designed for serializing and deserializing classes in the
 * {@code vector} package.
 */
public final class VectorConfigProcessors {
    private static final ConfigProcessor<Vec3D> vec3D = new ConfigProcessor<>() {
        @Override
        public @NotNull Vec3D dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            double x = element.getNumberOrThrow("x").doubleValue();
            double y = element.getNumberOrThrow("y").doubleValue();
            double z = element.getNumberOrThrow("z").doubleValue();
            return Vec3D.immutable(x, y, z);
        }

        @Override
        public @NotNull ConfigElement elementFromData(Vec3D vec3D) {
            ConfigNode node = new LinkedConfigNode(3);
            node.putNumber("x", vec3D.x());
            node.putNumber("y", vec3D.y());
            node.putNumber("z", vec3D.z());
            return node;
        }
    };

    private static final ConfigProcessor<Vec3I> vec3I = new ConfigProcessor<>() {
        @Override
        public @NotNull Vec3I dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            int x = element.getNumberOrThrow("x").intValue();
            int y = element.getNumberOrThrow("y").intValue();
            int z = element.getNumberOrThrow("z").intValue();
            return Vec3I.immutable(x, y, z);
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull Vec3I vec) {
            ConfigNode node = new LinkedConfigNode(3);
            node.putNumber("x", vec.x());
            node.putNumber("y", vec.y());
            node.putNumber("z", vec.z());
            return node;
        }
    };

    private static final ConfigProcessor<Bounds3I> bounds3I = new ConfigProcessor<>() {
        @Override
        public Bounds3I dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Vec3I origin = vec3I.dataFromElement(element.getElementOrThrow("origin"));
            Vec3I lengths = vec3I.dataFromElement(element.getElementOrThrow("lengths"));
            return Bounds3I.immutable(origin, lengths);
        }

        @Override
        public @NotNull ConfigElement elementFromData(Bounds3I region3I) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(2);
            node.put("origin",
                    vec3I.elementFromData(Vec3I.immutable(region3I.originX(), region3I.originY(), region3I.originZ())));
            node.put("lengths",
                    vec3I.elementFromData(Vec3I.immutable(region3I.lengthX(), region3I.lengthY(), region3I.lengthZ())));
            return node;
        }
    };

    private static final ConfigProcessor<Bounds3D> bounds3D = new ConfigProcessor<>() {
        @Override
        public Bounds3D dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Vec3D origin = vec3D.dataFromElement(element.getElementOrThrow("origin"));
            Vec3D lengths = vec3D.dataFromElement(element.getElementOrThrow("lengths"));
            return Bounds3D.immutable(origin, lengths);
        }

        @Override
        public @NotNull ConfigElement elementFromData(Bounds3D region3D) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(2);
            node.put("origin",
                    vec3D.elementFromData(Vec3D.immutable(region3D.originX(), region3D.originY(), region3D.originZ())));
            node.put("lengths",
                    vec3D.elementFromData(Vec3D.immutable(region3D.lengthX(), region3D.lengthY(), region3D.lengthZ())));
            return node;
        }
    };

    /**
     * Returns the common {@link ConfigProcessor} used to serialize/deserialize {@link Vec3I} instances.
     *
     * @return the ConfigProcessor used to serialize/deserialize Vec3I instances
     */
    public static @NotNull ConfigProcessor<Vec3I> vec3I() {
        return vec3I;
    }

    /**
     * Returns the common {@link ConfigProcessor} used to serialize/deserialize {@link Vec3D} instances.
     *
     * @return the ConfigProcessor used to serialize/deserialize Vec3D instances
     */
    public static @NotNull ConfigProcessor<Vec3D> vec3D() {
        return vec3D;
    }

    /**
     * Returns the common {@link ConfigProcessor} used to serialize/deserialize {@link Bounds3I} instances.
     *
     * @return the ConfigProcessor used to serialize/deserialize Region3I instances
     */
    public static @NotNull ConfigProcessor<Bounds3I> bounds3I() {
        return bounds3I;
    }

    /**
     * Returns the common {@link ConfigProcessor} used to serialize/deserialize {@link Bounds3D} instances.
     *
     * @return the ConfigProcessor used to serialize/deserialize Bounds3D instances
     */
    public static ConfigProcessor<Bounds3D> bounds3D() {
        return bounds3D;
    }
}
