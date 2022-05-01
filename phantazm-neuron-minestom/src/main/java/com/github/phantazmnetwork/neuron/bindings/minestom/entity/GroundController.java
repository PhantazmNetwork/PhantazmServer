package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.commons.MathUtils;
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
    private static final float JUMP_TOLERANCE = 0.125F;

    private final Entity entity;
    private final double speed;
    private final double step;

    private boolean jumping;

    public GroundController(@NotNull Entity entity, float step, double walkSpeed) {
        this.entity = Objects.requireNonNull(entity, "entity");
        this.speed = walkSpeed;
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
        double vX = Math.cos(radians) * speed;
        double vZ = Math.sin(radians) * speed;

        //make sure speedX and speedZ cannot extend past the target
        double speedX = Math.copySign(Math.min(Math.abs(vX), Math.abs(dX)), dX);
        double speedZ = Math.copySign(Math.min(Math.abs(vZ), Math.abs(dZ)), dZ);

        int tps = MinecraftServer.TICK_PER_SECOND;
        if(!entity.hasVelocity()) {
            if(entity.isOnGround()) {
                if(!jumping) {
                    PhysicsResult physicsResult = CollisionUtils.handlePhysics(entity, new Vec(speedX, 0, speedZ));
                    Pos pos = physicsResult.newPosition().withView(PositionUtils.getLookYaw(dX, dZ), 0);

                    if(entityPos.y() < targetExact && PhysicsUtils.hasCollision(physicsResult)) {
                        Vec3I currentPos = current.getPosition();
                        double nodeDiff = targetExact - (currentPos.getY() + current.getHeightOffset());
                        if(nodeDiff > step) {
                            entity.setVelocity(new Vec(speedX, computeJumpVelocity(nodeDiff), speedZ).mul(tps));
                            jumping = true;
                        } else if(nodeDiff > -Vec.EPSILON && nodeDiff < step + Vec.EPSILON) {
                            entity.refreshPosition(entity.getPosition().add(speedX, nodeDiff, speedZ));
                            return;
                        }
                    }

                    entity.refreshPosition(pos);
                }
                else {
                    jumping = false;
                }
            }
        }
        else if(jumping) {
            System.out.println(entityPos);
            if(entityPos.y() > targetExact) {
                PhysicsResult physicsResult = CollisionUtils.handlePhysics(entity, new Vec(speedX, 0, speedZ));
                entity.refreshPosition(physicsResult.newPosition().withView(PositionUtils.getLookYaw(dX, dZ), 0));
                entity.setVelocity(Vec.ZERO);
                jumping = false;
            }
        }
    }

    //abandon hope, all ye who enter here expecting to understand how this works
    private double computeJumpVelocity(double h) {
        double b = entity.getGravityDragPerTick();
        double g = entity.getGravityAcceleration();

        if(b == 0) {
            return Math.sqrt(2 * g * h) + (g / (1 -b));
        }
        else {
            double l_0 = Math.log(1 - b);
            double x_0 = b * h * l_0;
            double x_1 = g * (b - 1);
            double x_2 = x_1 * Math.log(g - (b * g));
            double x_3 = x_1 * b;
            double x_4 = x_1 * l_0;
            double x_5 = (-b * g) + g;

            double exp = Math.exp((-x_0 + x_2 + x_3 + x_4 + x_5) / x_1);
            return ((x_1 * MathUtils.lambertW(-1, exp / x_1) - x_5) / b) + (g / (1 - b));
        }
    }
}