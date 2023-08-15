package org.phantazm.mob2.selector;

import com.github.steanky.element.core.annotation.FactoryMethod;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.Tags;
import org.phantazm.mob2.Keys;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;

import java.util.UUID;

public class LastHitSelector implements SelectorComponent {
    @FactoryMethod
    public LastHitSelector() {
    }

    @Override
    public @NotNull Selector apply(@NotNull InjectionStore injectionStore) {
        return new Internal(injectionStore.get(Keys.MOB_KEY));
    }

    private record Internal(Mob self) implements Selector {
        @Override
        public @NotNull Target select() {
            Instance instance = self.getInstance();
            if (instance == null) {
                return Target.NONE;
            }

            UUID lastHit = self.getTag(Tags.LAST_MELEE_HIT_TAG);
            if (lastHit == null) {
                return Target.NONE;
            }

            for (LivingEntity livingEntity : instance.getEntityTracker()
                    .entities(EntityTracker.Target.LIVING_ENTITIES)) {
                if (!livingEntity.getUuid().equals(lastHit)) {
                    continue;
                }

                if (livingEntity.isRemoved()) {
                    return Target.NONE;
                }

                return Target.entities(livingEntity);
            }

            return Target.NONE;
        }
    }
}
