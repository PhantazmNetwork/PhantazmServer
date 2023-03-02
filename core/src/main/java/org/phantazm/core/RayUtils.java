package org.phantazm.core;

import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Utility methods related to ray tracing.
 */
public class RayUtils {

    private RayUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Ray traces a {@link Shape} and finds an intersection position. This does not ray trace a {@link Shape}'s children.
     *
     * @param shape         The {@link Shape} to ray trace
     * @param shapeLocation The location of the {@link Shape} in an {@link Instance}
     * @param start         The start position of the ray
     * @return The intersection position if it exists, otherwise {@link Optional#empty()}
     */
    @SuppressWarnings({"DuplicatedCode", "UnstableApiUsage"})
    public static @NotNull Optional<Vec> rayTrace(@NotNull Shape shape, @NotNull Point shapeLocation,
            @NotNull Pos start) {
        Point shapeStart = shape.relativeStart();
        Point shapeEnd = shape.relativeEnd();
        double minX = shapeStart.x() + shapeLocation.x();
        double minY = shapeStart.y() + shapeLocation.y();
        double minZ = shapeStart.z() + shapeLocation.z();

        double maxX = shapeEnd.x() + shapeLocation.x();
        double maxY = shapeEnd.y() + shapeLocation.y();
        double maxZ = shapeEnd.z() + shapeLocation.z();

        double startX = start.x();
        double startY = start.y();
        double startZ = start.z();

        Vec direction = start.direction();
        double directionX = direction.x();
        double directionY = direction.y();
        double directionZ = direction.z();

        double divX = 1D / directionX;
        double divY = 1D / directionY;
        double divZ = 1D / directionZ;

        double minMultiplier, maxMultiplier;
        if (directionX >= 0D) {
            minMultiplier = (minX - startX) * divX;
            maxMultiplier = (maxX - startX) * divX;
        }
        else {
            minMultiplier = (maxX - startX) * divX;
            maxMultiplier = (minX - startX) * divX;
        }

        double minYMultiplier, maxYMultiplier;
        if (directionY >= 0D) {
            minYMultiplier = (minY - startY) * divY;
            maxYMultiplier = (maxY - startY) * divY;
        }
        else {
            minYMultiplier = (maxY - startY) * divY;
            maxYMultiplier = (minY - startY) * divY;
        }
        if ((minMultiplier > maxYMultiplier) || (maxMultiplier < minYMultiplier)) {
            return Optional.empty();
        }
        if (minYMultiplier > minMultiplier) {
            minMultiplier = minYMultiplier;
        }
        if (maxYMultiplier < maxMultiplier) {
            maxMultiplier = maxYMultiplier;
        }

        double minZMultiplier, maxZMultiplier;
        if (directionZ >= 0D) {
            minZMultiplier = (minZ - startZ) * divZ;
            maxZMultiplier = (maxZ - startZ) * divZ;
        }
        else {
            minZMultiplier = (maxZ - startZ) * divZ;
            maxZMultiplier = (minZ - startZ) * divZ;
        }
        if ((minMultiplier > maxZMultiplier) || (maxMultiplier < minZMultiplier)) {
            return Optional.empty();
        }
        if (minZMultiplier > minMultiplier) {
            minMultiplier = minZMultiplier;
        }
        if (maxZMultiplier < maxMultiplier) {
            maxMultiplier = maxZMultiplier;
        }

        if (maxMultiplier < 0D) {
            return Optional.empty();
        }

        double multiplier = minMultiplier < 0D ? maxMultiplier : minMultiplier;
        return Optional.of(direction.mul(multiplier).add(start));
    }

    /**
     * Finds the exact intersection position of a ray with a {@link Shape}. This does ray trace a {@link Shape}'s children.
     *
     * @param shape         The {@link Shape} to ray trace
     * @param shapeLocation The location of the {@link Shape} in an {@link Instance}
     * @param start         The start position of the ray
     * @return The intersection position if it exists, otherwise {@link Optional#empty()}
     */
    @SuppressWarnings("UnstableApiUsage")
    public static @NotNull Optional<Vec> getIntersectionPosition(@NotNull Shape shape, @NotNull Point shapeLocation,
            @NotNull Pos start) {
        return RayUtils.rayTrace(shape, shapeLocation, start).map(trace -> {
            if (shape.childBounds().isEmpty()) {
                return trace;
            }

            List<Vec> traces = new ArrayList<>(shape.childBounds().size() + 1);
            traces.add(trace);

            for (Shape child : shape.childBounds()) {
                rayTrace(child, shapeLocation, start).ifPresent(traces::add);
            }

            traces.sort(Comparator.comparingDouble(start::distanceSquared));
            return traces.get(0);
        });
    }

}
