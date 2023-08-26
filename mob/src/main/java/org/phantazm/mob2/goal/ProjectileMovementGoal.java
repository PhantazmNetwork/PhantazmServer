package org.phantazm.mob2.goal;

import net.minestom.server.MinecraftServer;
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
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.phantazm.proxima.bindings.minestom.goal.ProximaGoal;

import java.util.Collection;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class ProjectileMovementGoal implements ProximaGoal {
    private static final double MAGIC = 0.007499999832361937D; //what is this? nobody knows...

    private final Entity entity;
    private final Entity shooter;
    private final Point to;

    private final double power;
    private final double spread;

    private final Consumer<? super ProjectileCollideWithBlockEvent> blockCollideCallback;
    private final Consumer<? super ProjectileCollideWithEntityEvent> projectileCollideCallback;

    private final int selfCollisionTickThreshold;

    private Pos previousPos = null;
    private boolean collided = false;

    public ProjectileMovementGoal(@NotNull Entity entity, @NotNull Entity shooter, @NotNull Point to, double power,
        double spread, @NotNull Consumer<? super ProjectileCollideWithBlockEvent> blockCollideCallback,
        @NotNull Consumer<? super ProjectileCollideWithEntityEvent> projectileCollideCallback) {
        this.entity = Objects.requireNonNull(entity);
        this.shooter = Objects.requireNonNull(shooter);
        this.to = Objects.requireNonNull(to);
        this.power = power;
        this.spread = spread;
        this.blockCollideCallback = Objects.requireNonNull(blockCollideCallback);
        this.projectileCollideCallback = Objects.requireNonNull(projectileCollideCallback);

        //power is in blocks/s
        this.selfCollisionTickThreshold = (int) Math.ceil(
            ((entity.getBoundingBox().width() / 2) + (shooter.getBoundingBox().width() / 2) / power) *
                MinecraftServer.TICK_PER_SECOND);
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

        if (this.spread != 0) {
            Random random = ThreadLocalRandom.current();
            double spread = this.spread * MAGIC;
            dx += random.nextGaussian() * spread;
            dy += random.nextGaussian() * spread;
            dz += random.nextGaussian() * spread;
        }

        final double mul = MinecraftServer.TICK_PER_SECOND * power;
        entity.setVelocity(new Vec(dx * mul, dy * mul, dz * mul));
        entity.setView((float) Math.toDegrees(Math.atan2(dx, dz)),
            (float) Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
    }

    @Override
    public void tick(long time) {
        Pos currentPos = entity.getPosition();
        if (checkStuck(previousPos == null ? currentPos : previousPos, currentPos)) {
            if (collided) {
                return;
            }
            collided = true;
            entity.setVelocity(Vec.ZERO);
            entity.setNoGravity(true);
        } else if (collided) {
            collided = false;
            entity.setNoGravity(false);
            EventDispatcher.call(new ProjectileUncollideEvent(entity));
        }

        this.previousPos = currentPos;
    }

    private boolean checkStuck(Pos previousPos, Pos currentPos) {
        final Instance instance = entity.getInstance();
        if (instance == null) {
            return false;
        }

        Chunk chunk = entity.getChunk();
        if (chunk == null) {
            return false;
        }

        final long aliveTicks = entity.getAliveTicks();
        if (previousPos.samePoint(currentPos)) {
            Block block = instance.getBlock(previousPos);
            return block.isSolid() && !block.compare(Block.BARRIER) &&
                block.registry().collisionShape().intersectBox(
                    previousPos.sub(previousPos.blockX(), previousPos.blockY(), previousPos.blockZ()),
                    entity.getBoundingBox());
        }

        final BoundingBox bb = entity.getBoundingBox();

        final double part = bb.width() / 2;
        final Vec dir = currentPos.sub(previousPos).asVec();
        final int parts = (int) Math.ceil(dir.length() / part);
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
            if (block.isSolid() && !block.compare(Block.BARRIER) && block.registry().collisionShape()
                .intersectBox(previousPos.sub(blockPos.blockX(), blockPos.blockY(), blockPos.blockZ()),
                    entity.getBoundingBox())) {

                final ProjectileCollideWithBlockEvent event =
                    new ProjectileCollideWithBlockEvent(entity, previousPos, block);
                blockCollideCallback.accept(event);

                EventDispatcher.call(event);
                if (!event.isCancelled()) {
                    entity.teleport(previousPos);
                    return true;
                }
            }

            Collection<LivingEntity> entities = instance.getEntityTracker()
                .chunkEntities(chunk.getChunkX(), chunk.getChunkZ(),
                    EntityTracker.Target.LIVING_ENTITIES);

            for (Entity victim : entities) {
                if (victim == this.entity || (victim == shooter && aliveTicks < selfCollisionTickThreshold) ||
                    !bb.intersectEntity(previousPos, victim)) {
                    continue;
                }

                final ProjectileCollideWithEntityEvent event =
                    new ProjectileCollideWithEntityEvent(entity, previousPos, victim);
                projectileCollideCallback.accept(event);

                EventDispatcher.call(event);
                if (!event.isCancelled()) {
                    return collided || entity.isOnGround();
                }
            }
        }
        return false;
    }
}
