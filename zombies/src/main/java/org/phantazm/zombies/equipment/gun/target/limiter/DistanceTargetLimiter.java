package org.phantazm.zombies.equipment.gun.target.limiter;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.AttributeUtils;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.equipment.gun.Gun;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Model("zombies.gun.target_limiter.distance")
@Cache
public class DistanceTargetLimiter implements TargetLimiter {
    private final Data data;

    @FactoryMethod
    public DistanceTargetLimiter(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull List<Pair<? extends LivingEntity, Vec>> limitTargets(@NotNull Entity shooter, @NotNull Gun gun, @NotNull Pos start,
        @NotNull List<Pair<? extends LivingEntity, Vec>> targets) {
        List<Pair<? extends LivingEntity, Vec>> targetsCopy = new ArrayList<>(targets);
        Comparator<Pair<? extends LivingEntity, Vec>> comparator =
            Comparator.comparingDouble(pair -> start.distanceSquared(pair.value()));
        if (!data.prioritizeClosest()) {
            comparator = comparator.reversed();
        }

        targetsCopy.sort(comparator);

        int actualLimit;
        if (shooter instanceof LivingEntity livingEntity) {
            actualLimit = Math.round(AttributeUtils.computeWithBase(data.targetLimit, livingEntity
                .getAttribute(Attributes.BULLET_PENETRATION)));
        } else {
            actualLimit = data.targetLimit;
        }

        return targetsCopy.subList(0, Math.min(targets.size(), actualLimit));
    }

    @DataObject
    public record Data(int targetLimit,
        boolean prioritizeClosest) {

    }
}
