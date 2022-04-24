package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.bindings.minestom.PhysicsUtils;
import com.github.phantazmnetwork.neuron.navigator.Controller;
import com.github.phantazmnetwork.neuron.node.Node;
import com.google.gson.internal.reflect.ReflectionHelper;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.utils.position.PositionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GroundController implements Controller {
    private static final double STEP_EPSILON = 1E-5;

    private final Entity entity;
    private final double speed;
    private final double step;

    public GroundController(@NotNull Entity entity, float step, double speed) {
        this.entity = Objects.requireNonNull(entity, "entity");
        this.speed = speed;
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
        Vec3I targetPos = target.getPosition();
        Pos entityPos = entity.getPosition();

        double targetExact = targetPos.getY() + target.getHeightOffset();

        double dX = (targetPos.getX() + 0.5) - entityPos.x();
        double dY = targetExact - entityPos.y();
        double dZ = (targetPos.getZ() + 0.5) - entityPos.z();

        //slows down entities when they reach their position
        double distSquared = dX * dX + dY * dY + dZ * dZ;
        double speed = this.speed;
        if (speed > distSquared) {
            speed = distSquared;
        }

        double radians = Math.atan2(dZ, dX);
        double speedX = Math.cos(radians) * speed;
        double speedZ = Math.sin(radians) * speed;

        PhysicsResult physicsResult = CollisionUtils.handlePhysics(entity, new Vec(speedX, 0, speedZ));
        Pos pos = physicsResult.newPosition().withView(PositionUtils.getLookYaw(dX, dZ), 0);
        Pos dPos = pos.sub(entityPos);
        System.out.println("Pos before stepPos: " + entityPos);
        System.out.println("stepPos: " + dPos);

        if(PhysicsUtils.hasCollision(physicsResult) && entity.isOnGround() && entityPos.y() < targetExact) {
            Vec3I currentPos = current.getPosition();
            double nodeDiff = targetExact - (currentPos.getY() + current.getHeightOffset());

            if(nodeDiff > step) {
                entity.setVelocity(new Vec(speedX, nodeDiff, speedZ));
            }
            else if(nodeDiff > STEP_EPSILON && nodeDiff < step + STEP_EPSILON) {
                System.out.println("Pos before step: " + entityPos);
                entity.teleport(entityPos.add(speedX * 1.1, nodeDiff, speedZ * 1.1));
                System.out.println("Pos after step: " + entity.getPosition());
                System.out.println();
            }
        }
        else {
            System.out.println("Pos before refresh: " + entityPos);
            entity.refreshPosition(pos);
            System.out.println("Pos after refresh: " + entity.getPosition());
            System.out.println();
        }
    }
}