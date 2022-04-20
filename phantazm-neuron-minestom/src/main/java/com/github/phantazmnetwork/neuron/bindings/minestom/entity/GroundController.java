package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.navigator.Controller;
import com.github.phantazmnetwork.neuron.node.Node;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.utils.position.PositionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class GroundController implements Controller {
    private static final double EPSILON = 1E-5;

    private final Entity entity;
    private final double speed;
    private final double speedInverse;
    private final double step;

    public GroundController(@NotNull Entity entity, float step, double speed) {
        this.entity = Objects.requireNonNull(entity, "entity");
        this.speed = speed;
        this.speedInverse = 1 / speed;
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
        Vec3I currentPos = current.getPosition();
        Vec3I targetPos = target.getPosition();
        Pos position = entity.getPosition();

        double currentExact = currentPos.getY() + current.getHeightOffset();
        double targetExact = targetPos.getY() + target.getHeightOffset();

        double dx = (targetPos.getX() + 0.5) - position.x();
        double dy = targetExact - position.y();
        double dz = (targetPos.getZ() + 0.5) - position.z();

        //slows down entities when they reach their position
        double distSquared = dx * dx + dy * dy + dz * dz;
        double speed = this.speed;
        if (speed > distSquared) {
            speed = distSquared;
        }

        double radians = Math.atan2(dz, dx);
        double speedX = Math.cos(radians) * speed;
        double speedY = dy * speed;
        double speedZ = Math.sin(radians) * speed;

        float yaw = PositionUtils.getLookYaw(dx, dz);
        float pitch = PositionUtils.getLookPitch(dx, dy, dz);

        double diff = targetExact - currentExact;
        PhysicsResult physicsResult = CollisionUtils.handlePhysics(entity, new Vec(speedX, speedY, speedZ));
        Pos pos = physicsResult.newPosition().withView(yaw, pitch);

        if(diff - EPSILON <= step && diff > EPSILON && !entity.hasVelocity() && (physicsResult.collisionX() ||
                physicsResult.collisionY() || physicsResult.collisionZ())) {
            pos = pos.add(speedX, diff + EPSILON, speedZ);
        }

        entity.refreshPosition(pos);

        if(diff > step && entity.isOnGround() && !entity.hasVelocity()) {
            entity.setVelocity(new Vec(0, dy * speedInverse, 0));
        }
    }
}