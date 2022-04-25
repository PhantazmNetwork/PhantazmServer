package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.bindings.minestom.PhysicsUtils;
import com.github.phantazmnetwork.neuron.navigator.Controller;
import com.github.phantazmnetwork.neuron.node.Node;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.utils.position.PositionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GroundController implements Controller {
    private static final double EPSILON = 1E-5;

    private final Entity entity;
    private final double speed;
    private final double step;

    private boolean jumping;

    public GroundController(@NotNull Entity entity, float step, double walkSpeed) {
        this.entity = Objects.requireNonNull(entity, "entity");
        this.speed = walkSpeed;
        this.step = step;
    }

    private double computeJumpVelocity(double gravitationalConstant, double requiredHeight) {
        return Math.sqrt(-2 * -gravitationalConstant * (requiredHeight + entity.getGravityAcceleration() * 4));
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
        double speedZ = Math.sin(radians) * speed;

        if(!entity.hasVelocity()) {
            if (entity.isOnGround()) {
                PhysicsResult physicsResult = CollisionUtils.handlePhysics(entity, new Vec(speedX, 0, speedZ));
                Pos pos = physicsResult.newPosition().withView(PositionUtils.getLookYaw(dX, dZ), 0);

                if (PhysicsUtils.hasCollision(physicsResult) && entityPos.y() < targetExact) {
                    Vec3I currentPos = current.getPosition();
                    double nodeDiff = targetExact - (currentPos.getY() + current.getHeightOffset());
                    if (nodeDiff > step) {
                        entity.setVelocity(new Vec(speedX, computeJumpVelocity(entity.getGravityAcceleration(),
                                nodeDiff), speedZ).mul(MinecraftServer.TICK_PER_SECOND));
                        jumping = true;
                    } else if (nodeDiff > -EPSILON && nodeDiff < step + EPSILON) {
                        entity.refreshPosition(entity.getPosition().add(speedX, nodeDiff, speedZ));
                        return;
                    }
                }

                entity.refreshPosition(pos);
            }
        }
        else if(jumping) {
            if(entityPos.y() > targetExact - EPSILON) {
                entity.refreshPosition(new Pos(entityPos.x() + speedX, targetExact, entityPos.z() + speedZ)
                        .withView(PositionUtils.getLookYaw(dX, dZ), 0));
                entity.setVelocity(Vec.ZERO);
                jumping = false;
            }
        }
    }
}