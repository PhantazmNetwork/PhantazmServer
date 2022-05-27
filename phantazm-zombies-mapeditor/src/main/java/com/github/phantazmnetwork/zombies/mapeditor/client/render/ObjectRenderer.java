package com.github.phantazmnetwork.zombies.mapeditor.client.render;

import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.key.Key;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;

public interface ObjectRenderer {
    float EPSILON = 1E-3F;
    float DOUBLE_EPSILON = EPSILON * 2;

    enum RenderType {
        FILLED,
        OUTLINE
    }

    final class RenderObject {
        public final Key key;
        public RenderType type;
        public Color color;
        public boolean shouldRender;
        public boolean renderThroughWalls;

        public Vec3d[] bounds;

        public RenderObject(@NotNull Key key, @NotNull RenderType type, @NotNull Color color, boolean shouldRender,
                            boolean renderThroughWalls, Vec3d ... bounds) {
            this.key = Objects.requireNonNull(key, "key");
            this.type = Objects.requireNonNull(type, "type");
            this.color = Objects.requireNonNull(color, "color");
            this.shouldRender = shouldRender;
            this.renderThroughWalls = renderThroughWalls;
            this.bounds = Objects.requireNonNull(bounds, "bounds");
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (obj instanceof RenderObject other) {
                return key.equals(other.key);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }

    void removeObject(@NotNull Key key);

    void putObject(@NotNull RenderObject value);

    void setRenderThroughWalls(boolean rendersThroughWalls);

    void setEnabled(boolean enabled);

    boolean hasObject(@NotNull Key key);

    boolean isEnabled();

    int size();

    void clear();
}
