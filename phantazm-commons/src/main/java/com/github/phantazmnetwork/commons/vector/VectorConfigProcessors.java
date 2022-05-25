package com.github.phantazmnetwork.commons.vector;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

public final class VectorConfigProcessors {
    private static final String X_COMPONENT_STRING = "x";
    private static final String Y_COMPONENT_STRING = "y";
    private static final String Z_COMPONENT_STRING = "z";

    private static final ConfigProcessor<Vec3D> vec3D = new ConfigProcessor<>() {
        @Override
        public @NotNull Vec3D dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            double x = element.getNumberOrThrow(X_COMPONENT_STRING).doubleValue();
            double y = element.getNumberOrThrow(Y_COMPONENT_STRING).doubleValue();
            double z = element.getNumberOrThrow(Z_COMPONENT_STRING).doubleValue();
            return Vec3D.of(x, y, z);
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull Vec3D vec3D) {
            ConfigNode node = new LinkedConfigNode();
            node.put(X_COMPONENT_STRING, new ConfigPrimitive(vec3D.getX()));
            node.put(Y_COMPONENT_STRING, new ConfigPrimitive(vec3D.getY()));
            node.put(Z_COMPONENT_STRING, new ConfigPrimitive(vec3D.getZ()));
            return node;
        }
    };

    private static final ConfigProcessor<Vec3I> vec3I = new ConfigProcessor<>() {
        @Override
        public @NotNull Vec3I dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            int x = element.getNumberOrThrow(X_COMPONENT_STRING).intValue();
            int y = element.getNumberOrThrow(Y_COMPONENT_STRING).intValue();
            int z = element.getNumberOrThrow(Z_COMPONENT_STRING).intValue();
            return Vec3I.of(x, y, z);
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull Vec3I vec) {
            ConfigNode node = new LinkedConfigNode();
            node.put(X_COMPONENT_STRING, new ConfigPrimitive(vec.getX()));
            node.put(Y_COMPONENT_STRING, new ConfigPrimitive(vec.getY()));
            node.put(Z_COMPONENT_STRING, new ConfigPrimitive(vec.getZ()));
            return node;
        }
    };

    private static final ConfigProcessor<Vec3F> vec3F = new ConfigProcessor<>() {
        @Override
        public @NotNull Vec3F dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            float x = element.getNumberOrThrow(X_COMPONENT_STRING).floatValue();
            float y = element.getNumberOrThrow(Y_COMPONENT_STRING).floatValue();
            float z = element.getNumberOrThrow(Z_COMPONENT_STRING).floatValue();
            return Vec3F.of(x, y, z);
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull Vec3F vec) {
            ConfigNode node = new LinkedConfigNode();
            node.put(X_COMPONENT_STRING, new ConfigPrimitive(vec.getX()));
            node.put(Y_COMPONENT_STRING, new ConfigPrimitive(vec.getY()));
            node.put(Z_COMPONENT_STRING, new ConfigPrimitive(vec.getZ()));
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
}
