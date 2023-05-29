package org.phantazm.proxima.bindings.minestom.controller;

import com.github.steanky.proxima.node.Node;
import com.github.steanky.vector.Vec3D;
import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.position.PositionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.MathUtils;

import java.util.Objects;
import java.util.function.Predicate;

public class GroundController implements Controller {
    private final LivingEntity entity;
    private final double step;
    private final TrackerPredicate trackerPredicate;

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
        this.trackerPredicate = new TrackerPredicate();
    }

    private class TrackerPredicate implements Predicate<LivingEntity> {
        private Point entityPos;
        private double vX;
        private double vZ;

        private int count;
        private Vec3D sum;
        private int iterations;

        private TrackerPredicate() {
        }

        private void set(Point entityPos, double vX, double vZ) {
            this.entityPos = entityPos;
            this.vX = vX;
            this.vZ = vZ;

            this.count = 0;
            this.sum = null;
            this.iterations = 0;
        }

        @Override
        public boolean test(LivingEntity candidate) {
            if (candidate != entity && !(candidate instanceof Player) &&
                    candidate.getBoundingBox().intersectEntity(candidate.getPosition(), entity)) {
                count++;
                iterations++;

                Pos pos = candidate.getPosition();
                if (sum == null) {
                    sum = Vec3D.mutable(pos.x(), 0, pos.z());
                }
                else {
                    sum.add(pos.x(), 0, pos.z());
                }

                double dx = pos.x() - entityPos.x();
                double dz = pos.z() - entityPos.z();

                double length = Math.sqrt(dx * dx + dz * dz);
                double scaleFactor = 1D / Math.max(0.3, length);

                Vec oldVelocity = entity.getVelocity();

                if (length > 0.001) {
                    candidate.setVelocity(new Vec(dx * scaleFactor, oldVelocity.y(), dz * scaleFactor));
                }
                else {
                    double pZ = -vX;
                    if (iterations % 2 == 0) {
                        candidate.setVelocity(
                                new Vec(vZ * scaleFactor * MinecraftServer.TICK_PER_SECOND, oldVelocity.y(),
                                        pZ * scaleFactor * MinecraftServer.TICK_PER_SECOND));
                    }
                    else {
                        candidate.setVelocity(
                                new Vec(-vZ * scaleFactor * MinecraftServer.TICK_PER_SECOND, oldVelocity.y(),
                                        -pZ * scaleFactor * MinecraftServer.TICK_PER_SECOND));
                    }
                }
            }

            return iterations >= 5;
        }
    }

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

        double radians = Math.atan2(dZ, dX);

        double vX = Math.cos(radians) * speed;
        double vZ = Math.sin(radians) * speed;

        Instance instance = entity.getInstance();

        if (instance != null) {
            this.trackerPredicate.set(entityPos, vX, vZ);
            instance.getEntityTracker().nearbyEntitiesUntil(entityPos, entity.getBoundingBox().width(),
                    EntityTracker.Target.LIVING_ENTITIES, trackerPredicate);

            if (trackerPredicate.count > 0) {
                Vec3D average = trackerPredicate.sum;

                //average is now the vector from the average overlapping entity position to the current entity
                average.div(trackerPredicate.count).sub(entityPos.x(), 0, entityPos.z());

                double length = average.lengthSquared();
                if (length < entity.getEntityType().width() * entity.getEntityType().width()) {
                    double scaleFactor = 1D / Math.max(0.3, length);
                    entity.setVelocity(new Vec(-average.x() * scaleFactor * MinecraftServer.TICK_PER_SECOND,
                            entity.getVelocity().y(), -average.z() * scaleFactor * MinecraftServer.TICK_PER_SECOND));
                }
            }
        }

        //make sure speedX and speedZ cannot extend past the target
        double speedX = Math.copySign(Math.min(Math.abs(vX), Math.abs(dX)), dX);
        double speedZ = Math.copySign(Math.min(Math.abs(vZ), Math.abs(dZ)), dZ);

        if (jumping) {
            if (entityPos.y() > exactTargetY + Vec.EPSILON) {
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
            Vec deltaMove = new Vec(speedX, 0, speedZ);
            PhysicsResult physicsResult = CollisionUtils.handlePhysics(entity, deltaMove);
            Pos pos = physicsResult.newPosition().withView(PositionUtils.getLookYaw(dX, dZ), 0);

            if (entityPos.y() < exactTargetY && physicsResult.hasCollision()) {
                double nodeDiff = exactTargetY - (current.y + current.blockOffset);
                if (nodeDiff > step) {
                    entity.setVelocity(new Vec(speedX, computeJumpVelocity(nodeDiff), speedZ).mul(
                            MinecraftServer.TICK_PER_SECOND));
                    jumping = true;
                }
                else if (nodeDiff > -Vec.EPSILON && nodeDiff < step + Vec.EPSILON) {
                    if (instance != null) {
                        PhysicsResult canStep =
                                CollisionUtils.handlePhysics(instance, entity.getChunk(), entity.getBoundingBox(),
                                        entity.getPosition().add(0, nodeDiff + Vec.EPSILON, 0), deltaMove, null);

                        if (canStep.hasCollision()) {
                            entity.refreshPosition(pos);
                            return;
                        }
                    }

                    entity.refreshPosition(entity.getPosition().add(speedX, nodeDiff, speedZ));
                    return;
                }
            }

            entity.refreshPosition(pos);
        }
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
