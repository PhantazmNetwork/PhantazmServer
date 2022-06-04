package com.github.phantazmnetwork.zombies.mapeditor.client.render;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.zombies.map.RegionInfo;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class RenderUtils {
    private RenderUtils() {
        throw new UnsupportedOperationException();
    }

    public static Vec3d @NotNull [] arrayFromRegions(@NotNull Collection<? extends RegionInfo> regions) {
        Vec3d[] boundsArray = new Vec3d[regions.size() * 2];

        int i = 0;
        for(RegionInfo info : regions) {
            arrayFromRegion(info, boundsArray, i);
            i += 2;
        }

        return boundsArray;
    }

    public static Vec3d @NotNull [] arrayFromRegion(@NotNull RegionInfo info, Vec3d @NotNull [] boundsArray,
                                                    int offset) {
        Vec3I origin = info.origin();
        Vec3I lengths = info.lengths();

        boundsArray[offset] = new Vec3d(origin.getX() - ObjectRenderer.EPSILON, origin.getY() - ObjectRenderer
                .EPSILON, origin.getZ() - ObjectRenderer.EPSILON);
        boundsArray[offset + 1] = new Vec3d(lengths.getX() + ObjectRenderer.DOUBLE_EPSILON, lengths.getY() +
                ObjectRenderer.DOUBLE_EPSILON, lengths.getZ() + ObjectRenderer.DOUBLE_EPSILON);

        return boundsArray;
    }
}
