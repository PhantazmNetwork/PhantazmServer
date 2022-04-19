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

import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public class GroundController implements Controller {
    private final Entity entity;
    private final float jumpHeight;
    private final double speed;

    public GroundController(@NotNull Entity entity, float jumpHeight, double speed) {
        this.entity = Objects.requireNonNull(entity, "entity");
        this.jumpHeight = jumpHeight;
        this.speed = speed;
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
    public void advance(@NotNull Node node) {
        Vec3I vec3I = node.getPosition();
        Pos position = entity.getPosition();

        double exactHeight = vec3I.getY() + node.getHeightOffset();

        double dx = (vec3I.getX() + 0.5) - position.x();
        double dy = (exactHeight) - position.y();
        double dz = (vec3I.getZ() + 0.5) - position.z();

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

        PhysicsResult physicsResult = CollisionUtils.handlePhysics(entity, new Vec(speedX, speedY, speedZ));
        entity.refreshPosition(physicsResult.newPosition().withView(yaw, pitch));

        if(entity.getPosition().y() < exactHeight) {
            entity.setVelocity(new Vec(0, jumpHeight * 2.5F, 0));
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private boolean collides(PhysicsResult result) {
        return result.collisionX() || result.collisionY() || result.collisionZ();
    }
}
