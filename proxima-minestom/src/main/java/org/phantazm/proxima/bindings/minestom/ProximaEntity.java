package org.phantazm.proxima.bindings.minestom;

import com.github.steanky.proxima.Navigator;
import com.github.steanky.proxima.node.Node;
import com.github.steanky.proxima.path.PathResult;
import com.github.steanky.proxima.path.PathTarget;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.core.VecUtils;
import org.phantazm.proxima.bindings.minestom.controller.Controller;
import org.phantazm.proxima.bindings.minestom.goal.GoalGroup;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * An entity with navigation capabilities based on the Proxima library.
 */
public class ProximaEntity extends LivingEntity {
    private static final double NODE_REACH_DISTANCE = 0.4;

    protected final Pathfinding pathfinding;
    protected final List<GoalGroup> goalGroups;

    private Entity targetEntity;

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

    private int removalAnimationDelay = 1000;

    public ProximaEntity(@NotNull EntityType entityType, @NotNull UUID uuid, @NotNull Pathfinding pathfinding) {
        super(entityType, uuid);
        this.pathfinding = Objects.requireNonNull(pathfinding, "pathfinding");
        this.goalGroups = new ArrayList<>(5);
    }

    public @NotNull Pathfinding pathfinding() {
        return pathfinding;
    }

    public void setRemovalAnimationDelay(int delay) {
        this.removalAnimationDelay = delay;
    }

    private void cancelPath() {
        this.destination = null;
        pathfinding.getNavigator().cancel();

        targetEntity = null;
        destination = null;
        currentPath = null;

        current = null;
        target = null;

        recalculationDelay = 0;
        lastPathfind = 0;
        lastMoved = 0;

        lastX = 0;
        lastY = 0;
        lastZ = 0;
    }

    public void setDestination(@Nullable PathTarget destination) {
        if (destination == null && this.destination != null) {
            cancelPath();
            return;
        }

        if (this.destination == destination) {
            return;
        }

        this.destination = destination;
    }

    public <T extends Entity> void setDestination(@Nullable T targetEntity) {
        if (this.targetEntity == targetEntity) {
            return;
        }

        if (targetEntity == null || !targetEntity.isRemoved()) {
            this.targetEntity = targetEntity;
        }

        if (targetEntity == null) {
            cancelPath();
            return;
        }

        this.destination = PathTarget.resolving(() -> {
            if (!pathfinding.isValidTarget(targetEntity)) {
                return null;
            }

            return VecUtils.toDouble(targetEntity.getPosition());
        }, pathfinding.positionResolverForTarget(targetEntity), pathfinding.targetChangePredicate(targetEntity));
    }

    public @Nullable Entity getTargetEntity() {
        return targetEntity;
    }

    public void attack(@NotNull Entity target, boolean swingHand) {
        if (swingHand) {
            swingMainHand();
        }

        EventDispatcher.call(new EntityAttackEvent(this, target));
    }

    public void attack(@NotNull Entity target) {
        attack(target, false);
    }

    /**
     * Adds a {@link GoalGroup} to this entity.
     *
     * @param group The {@link GoalGroup} to add
     */
    public void addGoalGroup(@NotNull GoalGroup group) {
        Objects.requireNonNull(group, "group");
        goalGroups.add(group);
    }

    @Override
    public void update(long time) {
        super.update(time);

        navigatorTick(time);
        aiTick(time);
    }

    @Override
    public void kill() {
        super.kill();

        if (removalAnimationDelay > 0) {
            scheduleRemove(Duration.of(removalAnimationDelay, TimeUnit.MILLISECOND));
        }
        else {
            remove();
        }
    }

    @Override
    public void remove() {
        super.remove();
        cancelPath();
    }

    protected boolean canNavigate() {
        return !isDead() && getInstance() != null;
    }

    protected void navigatorTick(long time) {
        if (!canNavigate()) {
            return;
        }

        Navigator navigator = pathfinding.getNavigator();

        if (targetEntity != null && !pathfinding.isValidTarget(targetEntity)) {
            targetEntity = null;
            cancelPath();
            return;
        }

        if (targetEntity != null && getDistanceSquared(targetEntity) < 100) {
            lookAt(targetEntity);
        }

        if (navigator.navigationComplete()) {
            currentPath = navigator.getResult();
            if (!initPath(currentPath)) {
                currentPath = null;
            }
        }
        else if (destination != null && pathfinding.canPathfind(this) &&
                (time - lastPathfind > recalculationDelay && destination.hasChanged())) {
            navigator.navigate(position.x(), position.y(), position.z(), destination);
            this.lastPathfind = time;
        }

        if (currentPath != null && current != null && moveAlongPath(time)) {
            cancelPath();
        }
    }

    protected void aiTick(long time) {
        for (GoalGroup group : goalGroups) {
            group.tick(time);
        }
    }

    protected boolean initPath(@NotNull PathResult pathResult) {
        recalculationDelay = pathfinding.recalculationDelay(pathResult);

        Node head = pathResult.head();
        if (head == null) {
            return false;
        }

        Node node = head;
        Point currentPosition = getPosition();

        double closestNodeDistance = Double.POSITIVE_INFINITY;
        Node closestNode = null;

        while (node != null) {
            double thisDistance =
                    currentPosition.distanceSquared(node.x + 0.5, node.y + node.blockOffset, node.z + 0.5);

            if (thisDistance < closestNodeDistance) {
                closestNodeDistance = thisDistance;
                closestNode = node;
            }

            if (thisDistance < 1) {
                break;
            }

            node = node.parent;
        }

        assert closestNode != null;

        current = closestNode;

        Node currentParent = closestNode.parent;
        target = currentParent == null ? closestNode : currentParent;

        return true;
    }

    protected boolean withinDistance(@NotNull Node node) {
        Pos position = getPosition();
        return position.distanceSquared(new Vec(node.x + 0.5, node.y + node.blockOffset, node.z + 0.5)) <
                NODE_REACH_DISTANCE;
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

            controller.advance(current, target, targetEntity);

            lastX = currentX;
            lastY = currentY;
            lastZ = currentZ;
            return false;
        }

        return true;
    }
}
