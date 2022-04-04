package com.github.phantazmnetwork.neuron.bindings.minestom;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.navigator.Controller;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.utils.position.PositionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public class MinestomController implements Controller {
    private final Entity entity;
    private final double speed;

    public MinestomController(@NotNull Entity entity, double speed) {
        this.entity = Objects.requireNonNull(entity, "entity");
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

    @Override
    public void moveTo(@NotNull Vec3I vec3I) {
        //TODO copied from minestom's Navigator#moveTowards method, we're essentially replacing that functionality
        //TODO remove final abuse
        final Pos position = entity.getPosition();
        final Pos direction = new Pos(vec3I.getX() + 0.5, vec3I.getY(), vec3I.getZ() + 0.5);
        final double dx = direction.x() - position.x();
        final double dy = direction.y() - position.y();
        final double dz = direction.z() - position.z();
        // the purpose of these few lines is to slow down entities when they reach their destination
        final double distSquared = dx * dx + dy * dy + dz * dz;
        double speed = this.speed;
        if (speed > distSquared) {
            speed = distSquared;
        }
        final double radians = Math.atan2(dz, dx);
        final double speedX = Math.cos(radians) * speed;
        final double speedY = dy * speed;
        final double speedZ = Math.sin(radians) * speed;

        //TODO alternative to cringe @ApiStatus.Internal methods?
        final float yaw = PositionUtils.getLookYaw(dx, dz);
        final float pitch = PositionUtils.getLookPitch(dx, dy, dz);
        // Prevent ghosting
        final var physicsResult = CollisionUtils.handlePhysics(entity, new Vec(speedX, speedY, speedZ));
        this.entity.refreshPosition(physicsResult.newPosition().withView(yaw, pitch));
    }
}
