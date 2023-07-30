package org.phantazm.mob.target;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.Tags;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;

import java.util.Optional;
import java.util.UUID;

@Model("mob.selector.last_hit_entity")
@Cache
public class LastHitEntitySelector implements TargetSelector<LivingEntity> {
    @FactoryMethod
    public LastHitEntitySelector() {
    }

    @Override
    public @NotNull Optional<LivingEntity> selectTarget(@NotNull PhantazmMob self) {
        ProximaEntity entity = self.entity();
        UUID lastHit = entity.getTag(Tags.LAST_MELEE_HIT_TAG);
        Instance instance = entity.getInstance();

        if (lastHit == null || instance == null) {
            return Optional.empty();
        }

        for (LivingEntity livingEntity : instance.getEntityTracker().entities(EntityTracker.Target.LIVING_ENTITIES)) {
            if (livingEntity.getUuid().equals(lastHit)) {
                if (livingEntity.isRemoved()) {
                    return Optional.empty();
                }

                return Optional.of(livingEntity);
            }
        }
        return Optional.empty();
    }
}
