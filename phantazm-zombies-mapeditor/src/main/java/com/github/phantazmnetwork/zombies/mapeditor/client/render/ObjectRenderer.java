package com.github.phantazmnetwork.zombies.mapeditor.client.render;

import net.kyori.adventure.key.Key;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public interface ObjectRenderer {
    enum RenderType {
        FILLED,
        OUTLINE
    }

    record RenderObject(@NotNull Key key, @NotNull RenderType type, @NotNull Vec3d start, @NotNull Vec3d dimensions,
                        @NotNull Color color, boolean renderThroughWalls) {}

    void removeObject(@NotNull Key key);

    void setObject(@NotNull RenderObject value);

    void setRenderThroughWalls(boolean rendersThroughWalls);

    void setEnabled(boolean enabled);

    boolean isEnabled();

    int size();
}
