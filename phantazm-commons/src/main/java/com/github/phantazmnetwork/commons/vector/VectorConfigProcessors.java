package com.github.phantazmnetwork.commons.vector;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

public final class VectorConfigProcessors {
    private static final ConfigProcessor<Vec3D> vec3D = new ConfigProcessor<>() {
        @Override
        public @NotNull Vec3D dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            double x = element.getNumberOrThrow("x").doubleValue();
            double y = element.getNumberOrThrow("y").doubleValue();
            double z = element.getNumberOrThrow("z").doubleValue();
            return Vec3D.of(x, y, z);
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull Vec3D vec3D) {
            ConfigNode node = new LinkedConfigNode(3);
            node.putNumber("x", vec3D.getX());
            node.putNumber("y", vec3D.getY());
            node.putNumber("z", vec3D.getZ());
            return node;
        }
    };

    private static final ConfigProcessor<Vec3I> vec3I = new ConfigProcessor<>() {
        @Override
        public @NotNull Vec3I dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            int x = element.getNumberOrThrow("x").intValue();
            int y = element.getNumberOrThrow("y").intValue();
            int z = element.getNumberOrThrow("z").intValue();
            return Vec3I.of(x, y, z);
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull Vec3I vec) {
            ConfigNode node = new LinkedConfigNode(3);
            node.putNumber("x", vec.getX());
            node.putNumber("y", vec.getY());
            node.putNumber("z", vec.getZ());
            return node;
        }
    };

    private static final ConfigProcessor<Vec3F> vec3F = new ConfigProcessor<>() {
        @Override
        public @NotNull Vec3F dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            float x = element.getNumberOrThrow("x").floatValue();
            float y = element.getNumberOrThrow("y").floatValue();
            float z = element.getNumberOrThrow("z").floatValue();
            return Vec3F.of(x, y, z);
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull Vec3F vec) {
            ConfigNode node = new LinkedConfigNode(3);
            node.putNumber("x", vec.getX());
            node.putNumber("y", vec.getY());
            node.putNumber("z", vec.getZ());
            return node;
        }
    };

    private static final ConfigProcessor<Region3I> region3I = new ConfigProcessor<>() {
        @Override
        public Region3I dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Vec3I origin = vec3I.dataFromElement(element.getElementOrThrow("origin"));
            Vec3I lengths = vec3I.dataFromElement(element.getElementOrThrow("lengths"));
            return Region3I.normalized(origin, lengths);
        }

        @Override
        public @NotNull ConfigElement elementFromData(Region3I region3I) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(2);
            node.put("origin", vec3I.elementFromData(region3I.getOrigin()));
            node.put("lengths", vec3I.elementFromData(region3I.getLengths()));
            return node;
        }
    };

    public static @NotNull ConfigProcessor<Vec3I> vec3I() {
        return vec3I;
    }

    public static @NotNull ConfigProcessor<Vec3D> vec3D() {
        return vec3D;
    }

    public static @NotNull ConfigProcessor<Vec3F> vec3F() {
        return vec3F;
    }

    public static @NotNull ConfigProcessor<Region3I> region3I() { return region3I; }
}
