package org.phantazm.mob.goal;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.ProjectileMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityShootEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.entity.projectile.ProjectileUncollideEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.phantazm.proxima.bindings.minestom.goal.ProximaGoal;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProjectileMovementGoal implements ProximaGoal {
    private static final int COLLISION_TICK_THRESHOLD = 3;

    private final Entity entity;

    private final Entity shooter;

    private final Point to;

    private final double power;

    private final double spread;

    private Pos previousPos = null;

    private boolean collided = false;

    public ProjectileMovementGoal(@NotNull Entity entity, @NotNull Entity shooter, @NotNull Point to, double power,
            double spread) {
        this.entity = Objects.requireNonNull(entity, "entity");
        this.shooter = Objects.requireNonNull(shooter, "shooter");
        this.to = Objects.requireNonNull(to, "to");
        this.power = power;
        this.spread = spread;
    }

    @Override
    public boolean shouldStart() {
        return !entity.isRemoved();
    }

    @Override
    public boolean shouldEnd() {
        return entity.isRemoved();
    }

    @Override
    public void start() {
        entity.setHasPhysics(false);
        if (entity.getEntityMeta() instanceof ProjectileMeta projectileMeta) {
            projectileMeta.setShooter(shooter);
        }

        EntityShootEvent shootEvent = new EntityShootEvent(shooter, entity, to, power, spread);
        EventDispatcher.call(shootEvent);
        if (shootEvent.isCancelled()) {
            entity.remove();
            return;
        }

        previousPos = entity.getPosition();
        Pos from = this.shooter.getPosition().add(0D, this.shooter.getEyeHeight(), 0D);
        double dx = to.x() - from.x();
        double dy = to.y() - from.y();
        double dz = to.z() - from.z();

        final double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        dx /= length;
        dy /= length;
        dz /= length;
        Random random = ThreadLocalRandom.current();
        double spread = this.spread * 0.007499999832361937D;
        dx += random.nextGaussian() * spread;
        dy += random.nextGaussian() * spread;
        dz += random.nextGaussian() * spread;

        final double mul = 20 * power;
        entity.setVelocity(new Vec(dx * mul, dy * mul, dz * mul));
        entity.setView((float)Math.toDegrees(Math.atan2(dx, dz)),
                (float)Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
    }

    @Override
    public void tick(long time) {
        if (previousPos == null) {
            return;
        }

        Pos currentPos = entity.getPosition();
        if (checkStuck(previousPos, currentPos)) {
            if (collided) {
                return;
            }
            collided = true;
            entity.setVelocity(Vec.ZERO);
            entity.setNoGravity(true);
        }
        else if (collided) {
            collided = false;
            entity.setNoGravity(false);
            EventDispatcher.call(new ProjectileUncollideEvent(entity));
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private boolean checkStuck(Pos previousPos, Pos currentPos) {
        final Instance instance = entity.getInstance();
        if (instance == null) {
            return false;
        }
        final long aliveTicks = entity.getAliveTicks();
        if (previousPos.samePoint(currentPos)) {
            Block block = instance.getBlock(previousPos);
            return block.isSolid() && !block.compare(Block.BARRIER) && aliveTicks >= COLLISION_TICK_THRESHOLD &&
                    block.registry().collisionShape().intersectBox(
                            previousPos.sub(previousPos.blockX(), previousPos.blockY(), previousPos.blockZ()),
                            entity.getBoundingBox());
        }

        Chunk chunk = null;
        Collection<LivingEntity> entities = null;
        final BoundingBox bb = entity.getBoundingBox();

        final double part = bb.width() / 2;
        final Vec dir = currentPos.sub(previousPos).asVec();
        final int parts = (int)Math.ceil(dir.length() / part);
        final Pos direction = dir.normalize().mul(part).asPosition();

        Block block = null;
        Point blockPos = null;
        for (int i = 0; i < parts; ++i) {
            // If we're at last part, we can't just add another direction-vector, because we can exceed the end point.
            previousPos = (i == parts - 1) ? currentPos : previousPos.add(direction);
            if (block == null || !previousPos.sameBlock(blockPos)) {
                block = instance.getBlock(previousPos);
                blockPos = previousPos;
            }
            if (block.isSolid() && !block.compare(Block.BARRIER) && aliveTicks >= COLLISION_TICK_THRESHOLD &&
                    block.registry().collisionShape()
                            .intersectBox(previousPos.sub(blockPos.blockX(), blockPos.blockY(), blockPos.blockZ()),
                                    entity.getBoundingBox())) {

                final ProjectileCollideWithBlockEvent event =
                        new ProjectileCollideWithBlockEvent(entity, previousPos, block);
                EventDispatcher.call(event);
                if (!event.isCancelled()) {
                    entity.teleport(previousPos);
                    return true;
                }
            }
            if (entity.getChunk() != chunk) {
                chunk = entity.getChunk();
                entities = instance.getChunkEntities(chunk).stream().filter(entity -> entity instanceof LivingEntity)
                        .map(entity -> (LivingEntity)entity).collect(Collectors.toSet());
            }
            if (entities == null) {
                return false; // should never happen
            }
            Point finalPreviousPos = previousPos;
            Stream<LivingEntity> victimsStream = entities.stream()
                    .filter(entity -> bb.intersectEntity(finalPreviousPos, entity) && entity != this.entity);

            if (aliveTicks < COLLISION_TICK_THRESHOLD) {
                victimsStream = victimsStream.filter(entity -> entity != shooter);
            }
            final Optional<LivingEntity> victimOptional = victimsStream.findAny();
            if (victimOptional.isPresent()) {
                final LivingEntity target = victimOptional.get();
                final ProjectileCollideWithEntityEvent event =
                        new ProjectileCollideWithEntityEvent(entity, previousPos, target);
                EventDispatcher.call(event);
                if (!event.isCancelled()) {
                    return collided || entity.isOnGround();
                }
            }
        }
        return false;
    }
}
