package org.phantazm.proxima.bindings.minestom;

import com.github.steanky.proxima.Navigator;
import com.github.steanky.proxima.node.Node;
import com.github.steanky.proxima.path.PathResult;
import com.github.steanky.proxima.path.PathTarget;
import com.github.steanky.vector.Vec3D;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.proxima.bindings.minestom.controller.Controller;

import java.util.Objects;
import java.util.UUID;

public class ProximaEntity extends LivingEntity {
    private final Pathfinding pathfinding;

    private PathTarget destination;
    private PathResult currentPath;

    private Node current;
    private Node target;
    private boolean hasPath;

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

    @Override
    public void tick(long time) {
        Navigator navigator = pathfinding.getNavigator();

        if (navigator.navigationComplete()) {
            currentPath = navigator.getResult();
            if (!initPath(currentPath)) {
                currentPath = null;
            }
        }
        else if (destination != null && currentPath == null) {
            navigator.navigate(position.x(), position.y(), position.y(), destination);
            destination = null;
        }

        if (currentPath != null) {
            moveAlongPath(time);
        }

        super.tick(time);
    }

    protected boolean withinDistance(@NotNull Node node) {
        Pos position = getPosition();
        return node.x == position.blockX() && node.y == position.blockY() && node.z == position.blockZ();
    }

    protected boolean initPath(@NotNull PathResult pathResult) {
        if (!pathResult.isSuccessful()) {
            return false;
        }

        Node start = pathResult.nodes().get(0);

        current = start;

        Node currentParent = start.parent;
        target = currentParent == null ? start : currentParent;

        recalculationDelay = recalculationDelay(pathResult);
        hasPath = true;

        return true;
    }

    protected boolean moveAlongPath(long time) {
        Controller controller = pathfinding.getController(this);


        if (withinDistance(target)) {
            current = target;
            target = current.parent;
        }

        if (target != null) {
            double currentX = controller.getX();
            double currentY = controller.getY();
            double currentZ = controller.getZ();

            if (!controller.hasControl()) {
                if (!Vec3D.immutable(currentX, currentY, currentZ).equals(Vec3D.immutable(lastX, lastY, lastZ))) {
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

    protected long recalculationDelay(@NotNull PathResult pathResult) {
        return 0L;
    }

    protected void resetPath(long time) {
        current = null;
        target = null;
        hasPath = false;
        lastPathfind = time;
        lastMoved = time;
        lastX = 0;
        lastY = 0;
        lastZ = 0;
    }
}
