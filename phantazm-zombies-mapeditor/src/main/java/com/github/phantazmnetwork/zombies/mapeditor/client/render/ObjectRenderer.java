package com.github.phantazmnetwork.zombies.mapeditor.client.render;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public interface ObjectRenderer {
    enum RenderType {
        FILLED,
        OUTLINE
    }

    record RenderObject(@NotNull RenderType type, @NotNull Vec3d start, @NotNull Vec3d end, @NotNull Color color) {}

    void addObject(@NotNull RenderObject object);

    void removeObject(int index);

    void setObject(int index, @NotNull RenderObject value);

    void setRenderThroughWalls(boolean rendersThroughWalls);
}
