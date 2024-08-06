package org.phantazm.zombies.powerup;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class PowerupUtils {
    public static final BoundingBox POWERUP_BOUNDING_BOX = new BoundingBox(0.25, 0.0625, 0.25);
    public static final Vec DOWNWARD_SEARCH_VECTOR = new Vec(0, -10, 0);

    private PowerupUtils() {
    }

    public static @NotNull Point powerupSpawnPosition(@NotNull Instance instance, @NotNull Point point) {
        Objects.requireNonNull(instance);
        Objects.requireNonNull(point);

        Chunk chunk = instance.getChunkAt(point);
        if (chunk == null) {
            return point;
        }

        PhysicsResult result = CollisionUtils.handlePhysics(instance, chunk, POWERUP_BOUNDING_BOX, Pos.fromPoint(point),
            DOWNWARD_SEARCH_VECTOR, null);

        return result.hasCollision() ? result.newPosition() : point;
    }
}
