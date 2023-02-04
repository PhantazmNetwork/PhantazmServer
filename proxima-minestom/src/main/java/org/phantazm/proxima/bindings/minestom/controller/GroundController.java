package org.phantazm.proxima.bindings.minestom.controller;

import com.github.steanky.proxima.node.Node;
import com.github.steanky.toolkit.collection.Wrapper;
import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.position.PositionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.MathUtils;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class GroundController implements Controller {
    private final LivingEntity entity;
    private final double step;

    private boolean jumping;

    private double lastH = -1;
    private double lastB = -1;
    private double lastG = -1;

    private double lastJumpVelocity = -1;

    /**
     * Creates a new GroundController managing the provided entity, using the given step distance and walk speed.
     *
     * @param entity the entity managed by this controller
     * @param step   the maximum distance the entity may "step up" blocks
     */
    public GroundController(@NotNull LivingEntity entity, float step) {
        this.entity = Objects.requireNonNull(entity, "entity");
        this.step = step;
    }

    //this method's code is adapted from net.minestom.server.entity.pathfinding.Navigator#moveTowards(Point, double)
    @Override
    public void advance(@NotNull Node current, @NotNull Node target, @Nullable Entity targetEntity) {
        Pos entityPos = entity.getPosition();

        double exactTargetY = target.y + target.blockOffset + target.jumpOffset;

        double dX = (target.x + 0.5) - entityPos.x();
        double dZ = (target.z + 0.5) - entityPos.z();

        //slows down entities when they reach their position
        double distSquared = dX * dX + dZ * dZ;
        double speed = entity.getAttributeValue(Attribute.MOVEMENT_SPEED);
        if (speed > distSquared) {
            speed = distSquared;
        }

        int count = nearbyEntities();
        double radians = adjustAngle(count, Math.atan2(dZ, dX));

        speed = adjustSpeed(count, speed);
        double vX = Math.cos(radians) * speed;
        double vZ = Math.sin(radians) * speed;

        //make sure speedX and speedZ cannot extend past the target
        double speedX = Math.copySign(Math.min(Math.abs(vX), Math.abs(dX)), dX);
        double speedZ = Math.copySign(Math.min(Math.abs(vZ), Math.abs(dZ)), dZ);

        if (jumping) {
            if (entityPos.y() > exactTargetY + Vec.EPSILON) {
                Instance instance = entity.getInstance();
                Chunk chunk = entity.getChunk();

                assert instance != null;
                assert chunk != null;

                PhysicsResult physics = CollisionUtils.handlePhysics(instance, chunk, entity.getBoundingBox(),
                        new Pos(entityPos.x(), exactTargetY + Vec.EPSILON, entityPos.z()), new Vec(speedX, 0, speedZ),
                        null);

                if (!physics.hasCollision()) {
                    entity.refreshPosition(physics.newPosition().withView(PositionUtils.getLookYaw(dX, dZ), 0));
                    jumping = false;
                }
                return;
            }
            else if (entity.isOnGround()) {
                //jump failed (we're back on the ground)
                jumping = false;
            }
            else {
                //still jumping
                return;
            }
        }

        if (entity.isOnGround()) {
            PhysicsResult physicsResult = CollisionUtils.handlePhysics(entity, new Vec(speedX, 0, speedZ));
            Pos pos = physicsResult.newPosition().withView(PositionUtils.getLookYaw(dX, dZ), 0);

            if (entityPos.y() < exactTargetY && physicsResult.hasCollision()) {
                double nodeDiff = exactTargetY - (current.y + current.blockOffset);
                if (nodeDiff > step) {
                    entity.setVelocity(new Vec(speedX, computeJumpVelocity(nodeDiff), speedZ).mul(
                            MinecraftServer.TICK_PER_SECOND));
                    jumping = true;
                }
                else if (nodeDiff > -Vec.EPSILON && nodeDiff < step + Vec.EPSILON) {
                    entity.refreshPosition(entity.getPosition().add(speedX, nodeDiff, speedZ));
                    return;
                }
            }

            entity.refreshPosition(pos);
        }
    }

    private double adjustAngle(int count, double radians) {
        if (count == 0) {
            return radians;
        }

        return radians + ThreadLocalRandom.current().nextDouble(-Math.PI / 2, Math.PI / 2);
    }

    private double adjustSpeed(int count, double speed) {
        if (count == 0) {
            return speed;
        }

        return speed * (0.5 + Math.min(count / 20.0, 1));
    }

    private int nearbyEntities() {
        Instance instance = entity.getInstance();
        assert instance != null;

        Wrapper<Integer> countWrapper = Wrapper.of(0);
        Point position = entity.getPosition();
        instance.getEntityTracker()
                .nearbyEntitiesByChunkRange(position, 0, EntityTracker.Target.LIVING_ENTITIES, entity -> {
                    if (entity != this.entity && entity.getPosition().distanceSquared(position) < 1 &&
                            entity instanceof ProximaEntity) {
                        countWrapper.apply(c -> c + 1);
                    }
                });

        return countWrapper.get();
    }

    @Override
    public boolean hasControl() {
        return jumping;
    }

    //abandon hope, all ye who enter here expecting to understand how this works
    private double computeJumpVelocity(double h) {
        double b = entity.getGravityDragPerTick();
        double g = entity.getGravityAcceleration();

        //return a cached value if possible (lambertW can be expensive)
        if (h == lastH && b == lastB && g == lastG) {
            return lastJumpVelocity;
        }

        lastH = h;
        lastB = b;
        lastG = g;

        if (b == 0) {
            //when there's no drag to contend with, use a simple height formula
            return lastJumpVelocity = Math.sqrt(2 * g * h) + g;
        }
        else {
            /*
            calculate the precise required jump velocity using the inverse of the sum of two integrals A and -B, where
            A is the definite integral of the standard entity velocity formula in terms of time from 0 to the time at
            which we have reached our height (velocity is 0), and B is the definite integral from the time when we have
            reached our height (t) to (t + 1). the latter is necessary due to time being discrete and us needing to
            avoid underestimating height
             */
            double l0 = Math.log(1 - b);
            double x0 = b - 1;
            double x1 = -b * g + g;
            double x2 = 2 * b * g + g * l0 - g * Math.log(g);
            double x3 = b * l0;

            double z = -Math.exp((x0 * (-x1 - x2) - h * x3) / (g * x0)) / g;
            return lastJumpVelocity = (-g * MathUtils.lambertW(-1, z) - x1) / b;
        }
    }
}
