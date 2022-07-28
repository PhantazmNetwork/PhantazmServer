package com.github.phantazmnetwork.zombies.mapeditor.client.render;

import com.github.phantazmnetwork.commons.vector.Region3I;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Contains useful utilities for working with {@link ObjectRenderer.RenderObject}s
 */
public final class RenderUtils {
    private RenderUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts the given list of {@link Region3I} instances into a single array of {@link Vec3d} instances, for use in
     * creating a representative {@link ObjectRenderer.RenderObject}.
     *
     * @param regions the regions list
     * @param origin  the origin vector to which the regions are measured relative to
     * @return a flat array which can be used to construct a RenderObject
     */
    public static Vec3d @NotNull [] arrayFromRegions(@NotNull List<? extends Region3I> regions, @NotNull Vec3I origin) {
        Objects.requireNonNull(regions, "regions");
        Objects.requireNonNull(origin, "origin");

        Vec3d[] boundsArray = new Vec3d[regions.size() * 2];

        for (int i = 0; i < regions.size(); i++) {
            arrayFromRegion(regions.get(i), origin, boundsArray, i * 2);
        }

        return boundsArray;
    }

    /**
     * Converts a single {@link Region3I} into a pair of {@link Vec3d} objects, entering them into the given array
     * starting at the offset value. The region is considered relative to the given origin vector.
     *
     * @param region      the region to convert
     * @param origin      the origin to which region is relative to
     * @param boundsArray the Vec3d array to populate
     * @param offset      the offset vector to start entering values
     * @return {@code boundsArray}, for convenience
     */
    public static Vec3d @NotNull [] arrayFromRegion(@NotNull Region3I region, @NotNull Vec3I origin,
            Vec3d @NotNull [] boundsArray, int offset) {
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(boundsArray, "boundsArray");

        Vec3I infoOrigin = region.origin();
        Vec3I lengths = region.lengths();

        boundsArray[offset] = new Vec3d(infoOrigin.getX() + origin.getX() - ObjectRenderer.EPSILON,
                infoOrigin.getY() + origin.getY() - ObjectRenderer.EPSILON,
                infoOrigin.getZ() + origin.getZ() - ObjectRenderer.EPSILON);
        boundsArray[offset + 1] = new Vec3d(lengths.getX() + ObjectRenderer.DOUBLE_EPSILON,
                lengths.getY() + ObjectRenderer.DOUBLE_EPSILON, lengths.getZ() + ObjectRenderer.DOUBLE_EPSILON);

        return boundsArray;
    }
}
