package com.github.phantazmnetwork.zombies.mapeditor.client.render;

import it.unimi.dsi.fastutil.Pair;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public interface MultiBounds {
    Pair<Vec3d, Vec3d> @NotNull [] getBounds();
}
