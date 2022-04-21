package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.bindings.minestom.PhysicsUtils;
import com.github.phantazmnetwork.neuron.navigator.Controller;
import com.github.phantazmnetwork.neuron.node.Node;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.utils.position.PositionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GroundController implements Controller {
    private final Entity entity;
    private final double speed;
    private final double step;

    public GroundController(@NotNull Entity entity, float step, double speed) {
        this.entity = Objects.requireNonNull(entity, "entity");
        this.speed = speed;
        this.step = step;
    }

    @Override
    public double getX() {
        return entity.getPosition().x();
    }

    @Override
    public double getY() {
        return entity.getPosition().y();
    }

    @Override
    public double getZ() {
        return entity.getPosition().z();
    }

    //this method's code is adapted from net.minestom.server.entity.pathfinding.Navigator#moveTowards(Point, double)
    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void advance(@NotNull Node current, @NotNull Node target) {
        Vec3I targetPos = target.getPosition();
        Pos entityPos = entity.getPosition();

        double targetExact = targetPos.getY() + target.getHeightOffset();

        double dX = (targetPos.getX() + 0.5) - entityPos.x();
        double dY = targetExact - entityPos.y();
        double dZ = (targetPos.getZ() + 0.5) - entityPos.z();

        //slows down entities when they reach their position
        double distSquared = dX * dX + dY * dY + dZ * dZ;
        double speed = this.speed;
        if (speed > distSquared) {
            speed = distSquared;
        }

        double radians = Math.atan2(dZ, dX);
        double speedX = Math.cos(radians) * speed;
        double speedY = dY * speed;
        double speedZ = Math.sin(radians) * speed;

        PhysicsResult physicsResult = CollisionUtils.handlePhysics(entity, new Vec(speedX, speedY, speedZ));
        Pos pos = physicsResult.newPosition().withView(PositionUtils.getLookYaw(dX, dZ), 0);
        entity.refreshPosition(pos);

        if(PhysicsUtils.hasCollision(physicsResult)) {
            Vec3I currentPos = current.getPosition();
            double nodeDiff = targetExact - (currentPos.getY() + current.getHeightOffset());
            if(entity.getGravityTickCount() == 0 && pos.y() < targetExact) {
                if(nodeDiff > step) {
                    entity.setVelocity(new Vec(speedX, nodeDiff * 9, speedZ));
                }
                else {
                    entity.teleport(new Pos(pos.x() + speedX, pos.y() + nodeDiff, pos.z() + speedZ));
                }
            }
        }
    }
}