package org.phantazm.proxima.bindings.minestom;

import com.github.steanky.proxima.Navigator;
import com.github.steanky.proxima.node.Node;
import com.github.steanky.proxima.path.PathResult;
import com.github.steanky.proxima.path.PathTarget;
import com.github.steanky.vector.Vec3D;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.proxima.bindings.minestom.controller.Controller;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * An entity with navigation capabilities based on the Proxima library.
 */
public class ProximaEntity extends LivingEntity {
    private final Pathfinding pathfinding;

    private Supplier<Vec3D> movingTarget;
    private Vec3D lastTargetPosition;

    private PathTarget destination;
    private PathResult currentPath;

    private Node current;
    private Node target;

    private long recalculationDelay;
    private long lastPathfind;
    private long lastMoved;

    private double lastX;
    private double lastY;
    private double lastZ;

    public ProximaEntity(@NotNull EntityType entityType, @NotNull UUID uuid, @NotNull Pathfinding pathfinding) {
        super(entityType, uuid);
        this.pathfinding = Objects.requireNonNull(pathfinding, "pathfinding");
    }

    public @NotNull Pathfinding pathfinding() {
        return pathfinding;
    }

    public void setDestination(@NotNull PathTarget destination) {
        this.destination = Objects.requireNonNull(destination);
    }

    public void setTarget(@Nullable Supplier<Vec3D> movingTarget) {
        this.movingTarget = movingTarget;
    }

    @Override
    public void tick(long time) {
        Navigator navigator = pathfinding.getNavigator();

        if (navigator.navigationComplete()) {
            currentPath = navigator.getResult();
            if (!initPath(currentPath)) {
                currentPath = null;
            }
        }
        else if (destination != null && currentPath == null && time - lastPathfind < recalculationDelay) {
            navigator.navigate(position.x(), position.y(), position.y(), destination);
        }

        if (currentPath != null) {
            if (moveAlongPath(time)) {
                resetPath(time);
            }
        }

        super.tick(time);
    }

    protected boolean initPath(@NotNull PathResult pathResult) {
        recalculationDelay = pathfinding.recalculationDelay(pathResult);

        if (!pathResult.isSuccessful() || pathResult.nodes().isEmpty()) {
            return false;
        }

        Node start = pathResult.nodes().get(0);

        current = start;

        Node currentParent = start.parent;
        target = currentParent == null ? start : currentParent;
        return true;
    }

    protected boolean withinDistance(@NotNull Node node) {
        Pos position = getPosition();
        return node.x == position.blockX() && node.y == position.blockY() && node.z == position.blockZ();
    }

    protected boolean moveAlongPath(long time) {
        Controller controller = pathfinding.getController(this);

        if (withinDistance(target)) {
            current = target;
            target = current.parent;
        }

        if (target != null) {
            Point pos = getPosition();

            double currentX = pos.x();
            double currentY = pos.y();
            double currentZ = pos.z();

            if (!controller.hasControl()) {
                if (!(currentX == lastX && currentY == lastY && currentZ == lastZ)) {
                    lastMoved = time;
                }
                else if (time - lastMoved > pathfinding.immobileThreshold()) {
                    //if we don't have any movement, stop moving along this path
                    return true;
                }
            }
            else {
                //if jumping, keep updating lastMoved, so we don't consider ourselves stuck
                lastMoved = time;
            }

            controller.advance(current, target);

            lastX = currentX;
            lastY = currentY;
            lastZ = currentZ;
            return false;
        }

        return true;
    }

    protected void resetPath(long time) {
        current = null;
        target = null;
        lastPathfind = time;
        lastMoved = time;
        lastX = 0;
        lastY = 0;
        lastZ = 0;
    }
}
