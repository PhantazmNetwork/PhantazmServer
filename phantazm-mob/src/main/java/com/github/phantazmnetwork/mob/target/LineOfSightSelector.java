package com.github.phantazmnetwork.mob.target;

import com.github.phantazmnetwork.mob.PhantazmMob;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

public abstract class LineOfSightSelector<TReturn> extends NearestEntitySelector<TReturn> {

    public LineOfSightSelector(double range, int targetLimit) {
        super(range, targetLimit);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    protected boolean isTargetValid(@NotNull PhantazmMob mob, @NotNull Entity targetEntity, @NotNull TReturn target) {
        Vec start = mob.entity().getPosition().asVec();
        Vec dir = mob.entity().getPosition().direction();
        Pos pos = targetEntity.getPosition();

        return targetEntity.getBoundingBox().boundingBoxRayIntersectionCheck(start, dir, pos);
    }

}
