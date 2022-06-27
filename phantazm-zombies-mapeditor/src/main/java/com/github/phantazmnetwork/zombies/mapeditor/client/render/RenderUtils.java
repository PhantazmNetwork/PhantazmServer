package com.github.phantazmnetwork.zombies.mapeditor.client.render;

import com.github.phantazmnetwork.commons.vector.Region3I;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class RenderUtils {
    private RenderUtils() {
        throw new UnsupportedOperationException();
    }

    public static Vec3d @NotNull [] arrayFromRegions(@NotNull Collection<? extends Region3I> regions,
                                                     @NotNull Vec3I origin) {
        Vec3d[] boundsArray = new Vec3d[regions.size() * 2];

        int i = 0;
        for(Region3I region : regions) {
            arrayFromRegion(region, origin, boundsArray, i);
            i += 2;
        }

        return boundsArray;
    }

    public static Vec3d @NotNull [] arrayFromRegion(@NotNull Region3I region, @NotNull Vec3I origin,
                                                    Vec3d @NotNull [] boundsArray, int offset) {
        Vec3I infoOrigin = region.getOrigin();
        Vec3I lengths = region.getLengths();

        boundsArray[offset] = new Vec3d(infoOrigin.getX() + origin.getX() - ObjectRenderer.EPSILON, infoOrigin
                .getY() + origin.getY() - ObjectRenderer.EPSILON, infoOrigin.getZ() + origin.getZ() - ObjectRenderer
                .EPSILON);
        boundsArray[offset + 1] = new Vec3d(lengths.getX() + ObjectRenderer.DOUBLE_EPSILON, lengths.getY() +
                ObjectRenderer.DOUBLE_EPSILON, lengths.getZ() + ObjectRenderer.DOUBLE_EPSILON);

        return boundsArray;
    }
}
