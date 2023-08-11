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
    private static final double TARGET_EPSILON = 0.0001;
    private static final double ENTITY_COLLISION_FACTOR = 10;

    private final LivingEntity entity;
    private final double stepHeight;
    private final double jumpHeight;
    private final TrackerPredicate trackerPredicate;

    private boolean jumping;
    private double jumpTargetHeight;

    private double lastH = -1;
    private double lastB = -1;
    private double lastG = -1;

    private double lastJumpVelocity = -1;

    private int ticks;

    /**
     * Creates a new GroundController managing the provided entity, using the given step distance and walk speed.
     *
     * @param entity     the entity managed by this controller
     * @param stepHeight the maximum distance the entity may "step up" blocks
     */
    public GroundController(@NotNull LivingEntity entity, float stepHeight, float jumpHeight) {
        this.entity = Objects.requireNonNull(entity, "entity");
        this.stepHeight = stepHeight;
        this.jumpHeight = jumpHeight;
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
                        candidate.setVelocity(new Vec(vZ * scaleFactor * ENTITY_COLLISION_FACTOR, oldVelocity.y(),
                                pZ * scaleFactor * ENTITY_COLLISION_FACTOR));
                    }
                    else {
                        candidate.setVelocity(new Vec(-vZ * scaleFactor * ENTITY_COLLISION_FACTOR, oldVelocity.y(),
                                -pZ * scaleFactor * ENTITY_COLLISION_FACTOR));
                    }
                }
            }

            return iterations >= 5;
        }
    }

    @Override
    public void advance(@NotNull Node current, @NotNull Node target, @Nullable Entity targetEntity) {
        Chunk chunk = entity.getChunk();
        if (chunk == null) {
            return;
        }

        Pos entityPos = entity.getPosition();

        double dX = (target.x + 0.5) - entityPos.x();
        double dZ = (target.z + 0.5) - entityPos.z();

        //slows down entities when they reach their position
        double distSquared = dX * dX + dZ * dZ;
        if (distSquared < Vec.EPSILON) {
            return;
        }

        double speed = entity.getAttributeValue(Attribute.MOVEMENT_SPEED);
        if (speed > distSquared) {
            speed = distSquared;
        }

        double radians = Math.atan2(dZ, dX);

        double vX = Math.cos(radians) * speed;
        double vZ = Math.sin(radians) * speed;

        Instance instance = entity.getInstance();
        assert instance != null;

        if (ticks++ % 4 == 0) { //only do entity-entity collision every 4 ticks when moving
            this.trackerPredicate.set(entityPos, vX, vZ);
            instance.getEntityTracker().nearbyEntitiesUntil(entityPos, entity.getBoundingBox().width(),
                    EntityTracker.Target.LIVING_ENTITIES, trackerPredicate);

            if (trackerPredicate.count > 0) {
                Vec3D average = trackerPredicate.sum;

                //average is now the vector from the average overlapping entity position to the current entity
                average.div(trackerPredicate.count).sub(entityPos.x(), 0, entityPos.z());

                if (average.lengthSquared() < entity.getEntityType().width() * entity.getEntityType().width()) {
                    entity.setVelocity(new Vec(-average.x() * MinecraftServer.TICK_PER_SECOND, entity.getVelocity().y(),
                            -average.z() * MinecraftServer.TICK_PER_SECOND));
                }
            }
        }

        //make sure speedX and speedZ cannot extend past the target
        double speedX = Math.copySign(Math.min(Math.abs(vX), Math.abs(dX)), dX);
        double speedZ = Math.copySign(Math.min(Math.abs(vZ), Math.abs(dZ)), dZ);

        if (jumping) {
            if (entityPos.y() > jumpTargetHeight + Vec.EPSILON) {
                PhysicsResult physics = CollisionUtils.handlePhysics(instance, chunk, entity.getBoundingBox(),
                        new Pos(entityPos.x(), jumpTargetHeight + TARGET_EPSILON, entityPos.z()),
                        new Vec(speedX, 0, speedZ), null);

                if (!physics.hasCollision()) {
                    entity.refreshPosition(physics.newPosition().withView(PositionUtils.getLookYaw(dX, dZ), 0));
                    jumping = false;
                }
                else if (entity.isOnGround()) {
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

        if (!entity.isOnGround()) {
            return;
        }

        Vec deltaMove = new Vec(speedX, 0, speedZ);
        PhysicsResult physicsResult = CollisionUtils.handlePhysics(instance, chunk, entity.getBoundingBox(),
                new Pos(entityPos.x(), entityPos.y() + Vec.EPSILON, entityPos.z()), new Vec(speedX, 0, speedZ), null);

        Pos pos = physicsResult.newPosition().withView(PositionUtils.getLookYaw(dX, dZ), 0);

        double currentTarget = current.y + current.blockOffset;
        double exactTargetY = target.y + target.blockOffset + target.jumpOffset;
        if ((entityPos.y() < currentTarget || entityPos.y() < exactTargetY) && physicsResult.hasCollision()) {
            double currentDiff = currentTarget - entityPos.y() + TARGET_EPSILON;
            double targetDiff = exactTargetY - entityPos.y() + TARGET_EPSILON;

            boolean canReachCurrent = canReach(currentDiff);
            boolean canReachTarget = canReach(targetDiff);

            if (canReachTarget) {
                if (targetDiff > currentDiff) {
                    stepOrJump(targetDiff, exactTargetY, speedX, speedZ, chunk, instance, deltaMove, pos);
                    return;
                }
            }

            if (canReachCurrent) {
                stepOrJump(currentDiff, currentTarget, speedX, speedZ, chunk, instance, deltaMove, pos);
                return;
            }
        }

        entity.refreshPosition(pos);
    }

    private boolean canReach(double diff) {
        return diff < stepHeight + TARGET_EPSILON || diff < jumpHeight + TARGET_EPSILON;
    }

    private void stepOrJump(double nodeDiff, double target, double speedX, double speedZ, Chunk chunk,
            Instance instance, Vec deltaMove, Pos pos) {
        if (nodeDiff - TARGET_EPSILON < stepHeight && nodeDiff - TARGET_EPSILON < jumpHeight) {
            stepUp(instance, deltaMove, nodeDiff, pos, speedX, speedZ);
            return;
        }

        Pos entityPos = entity.getPosition();
        PhysicsResult physicsResult = CollisionUtils.handlePhysics(instance, chunk, entity.getBoundingBox(),
                new Pos(entityPos.x(), entityPos.y() + Vec.EPSILON + stepHeight, entityPos.z()),
                new Vec(speedX, 0, speedZ), null);
        if (!physicsResult.hasCollision()) {
            entity.refreshPosition(physicsResult.newPosition().withView(pos.yaw(), pos.pitch()));
            return;
        }

        entity.setVelocity(new Vec(speedX, computeJumpVelocity(nodeDiff), speedZ).mul(MinecraftServer.TICK_PER_SECOND));
        jumpTargetHeight = target + TARGET_EPSILON;
        jumping = true;
    }

    private void stepUp(Instance instance, Vec deltaMove, double nodeDiff, Pos pos, double speedX, double speedZ) {
        PhysicsResult canStep = CollisionUtils.handlePhysics(instance, entity.getChunk(), entity.getBoundingBox(),
                entity.getPosition().add(0, nodeDiff, 0), deltaMove, null);

        if (canStep.hasCollision()) {
            entity.refreshPosition(pos);
            return;
        }

        entity.refreshPosition(entity.getPosition().add(speedX, nodeDiff, speedZ));
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
