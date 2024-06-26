package org.phantazm.zombies.event.equipment;

import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.Gun;

import java.util.List;
import java.util.Objects;

public class GunTargetLimitEvent implements EntityInstanceEvent {
    private final Entity entity;
    private final Gun gun;
    private final List<Pair<? extends LivingEntity, Vec>> targets;

    private int targetLimit;

    public GunTargetLimitEvent(@NotNull Entity entity, @NotNull Gun gun,
        @NotNull List<Pair<? extends LivingEntity, Vec>> targets, int targetLimit) {
        this.entity = Objects.requireNonNull(entity);
        this.gun = Objects.requireNonNull(gun);
        this.targets = Objects.requireNonNull(targets);
        this.targetLimit = targetLimit;
    }

    @Override
    public @NotNull Entity getEntity() {
        return entity;
    }

    public @NotNull Gun gun() {
        return gun;
    }

    public @NotNull List<Pair<? extends LivingEntity, Vec>> targets() {
        return targets;
    }

    public int targetLimit() {
        return targetLimit;
    }

    public void setTargetLimit(int limit) {
        this.targetLimit = limit;
    }
}
