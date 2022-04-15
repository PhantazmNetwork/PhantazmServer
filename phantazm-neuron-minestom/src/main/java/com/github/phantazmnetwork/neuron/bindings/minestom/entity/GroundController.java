package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.commons.minestom.vector.VecUtils;
import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.navigator.Controller;
import com.github.phantazmnetwork.neuron.node.Node;
import com.github.phantazmnetwork.neuron.operation.PathResult;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.Block;
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

    @Override
    public double getVelocityX() {
        return entity.getVelocity().x();
    }

    @Override
    public double getVelocityY() {
        return entity.getVelocity().y();
    }

    @Override
    public double getVelocityZ() {
        return entity.getVelocity().z();
    }

    @Override
    public void setVelocity(@NotNull Vec3D velocity) {
        entity.setVelocity(new Vec(velocity.getX(), velocity.getY(), velocity.getZ()));
    }

    @Override
    public void init(PathResult result) {
        boolean first = true;
        for(Node node : result.getStart()) {
            Vec3I pos = node.getPosition();
            if(first) {
                entity.getInstance().setBlock(pos.getX(), pos.getY(), pos.getZ(), Block.OAK_SAPLING);
                first = false;
            }
            else {
                entity.getInstance().setBlock(pos.getX(), pos.getY(), pos.getZ(), Block.JUNGLE_SAPLING);
            }
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull Vec3D advance(@NotNull Vec3I vec3I) {
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
        float yaw = PositionUtils.getLookYaw(dx, dz);
        float pitch = PositionUtils.getLookPitch(dx, dy, dz);

        PhysicsResult physicsResult = CollisionUtils.handlePhysics(entity, new Vec(speedX, speedY, speedZ));
        Pos newPos = physicsResult.newPosition();
        entity.refreshPosition(newPos.withView(yaw, pitch));
        if(entity.getPosition().y() < vec3I.getY()) {
            entity.setVelocity(new Vec(0, 2.5F, 0));
        }

        return VecUtils.toDouble(newPos.sub(position));
    }
}
