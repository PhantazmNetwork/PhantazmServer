package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.navigator.Controller;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.utils.position.PositionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public class EntityController implements Controller {
    private final Entity entity;
    private final double speed;
    private final float jumpHeight;

    public EntityController(@NotNull Entity entity, double speed, float jumpHeight) {
        this.entity = Objects.requireNonNull(entity, "entity");
        this.speed = speed;
        this.jumpHeight = jumpHeight;
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

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void moveTo(@NotNull Vec3I vec3I) {
        Pos position = entity.getPosition();
        double dx = (vec3I.getX() + 0.5) - position.x();
        double dy = vec3I.getY() - position.y();
        double dz = (vec3I.getZ() + 0.5) - position.z();

        // the purpose of these few lines is to slow down entities when they reach their destination
        double distSquared = dx * dx + dy * dy + dz * dz;
        double speed = this.speed;
        if (speed > distSquared) {
            speed = distSquared;
        }
        double radians = Math.atan2(dz, dx);
        double speedX = Math.cos(radians) * speed;
        double speedY = dy * speed;
        double speedZ = Math.sin(radians) * speed;

        //TODO alternative to cringe @ApiStatus.Internal methods?
        float yaw = PositionUtils.getLookYaw(dx, dz);
        float pitch = PositionUtils.getLookPitch(dx, dy, dz);
        // Prevent ghosting
        PhysicsResult physicsResult = CollisionUtils.handlePhysics(entity, new Vec(speedX, speedY, speedZ));
        this.entity.refreshPosition(physicsResult.newPosition().withView(yaw, pitch));

        if(dy > 0 && dy <= jumpHeight) {
            jump((float) dy);
        }
    }

    private void jump(float height) {
        this.entity.setVelocity(new Vec(0, height * 2.5f, 0));
    }
}
