package com.github.phantazmnetwork.zombies.mapeditor.client.render;

import com.github.phantazmnetwork.commons.vector.Region3I;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class RenderUtils {
    private RenderUtils() {
        throw new UnsupportedOperationException();
    }

    public static Vec3d @NotNull [] arrayFromRegions(@NotNull List<? extends Region3I> regions,
                                                     @NotNull Vec3I origin) {
        Objects.requireNonNull(regions, "regions");
        Objects.requireNonNull(origin, "origin");

        Vec3d[] boundsArray = new Vec3d[regions.size() * 2];

        for(int i = 0; i < regions.size(); i++) {
            arrayFromRegion(regions.get(i), origin, boundsArray, i * 2);
        }

        return boundsArray;
    }

    public static Vec3d @NotNull [] arrayFromRegion(@NotNull Region3I region, @NotNull Vec3I origin,
                                                    Vec3d @NotNull [] boundsArray, int offset) {
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(boundsArray, "boundsArray");

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
