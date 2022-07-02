package com.github.phantazmnetwork.api;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class RayUtils {

    private RayUtils() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("DuplicatedCode")
    public static @NotNull Optional<Vec> rayTrace(@NotNull BoundingBox boundingBox, @NotNull Point boundingBoxPosition,
                                                  @NotNull Pos start) {
        double minX = boundingBox.minX() + boundingBoxPosition.x();
        double minY = boundingBox.minY() + boundingBoxPosition.y();
        double minZ = boundingBox.minZ() + boundingBoxPosition.z();
        double maxX = boundingBox.maxX() + boundingBoxPosition.x();
        double maxY = boundingBox.maxY() + boundingBoxPosition.y();
        double maxZ = boundingBox.maxZ() + boundingBoxPosition.z();

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

}
