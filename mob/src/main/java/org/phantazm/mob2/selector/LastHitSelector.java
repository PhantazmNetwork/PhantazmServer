package org.phantazm.mob2.selector;

import com.github.steanky.element.core.annotation.FactoryMethod;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.Tags;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;

import java.util.UUID;

public class LastHitSelector implements SelectorComponent {
    @FactoryMethod
    public LastHitSelector() {
    }

    @Override
    public @NotNull Selector apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Internal(mob);
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

            for (Entity entity : instance.getEntityTracker().entities(EntityTracker.Target.ENTITIES)) {
                if (!entity.getUuid().equals(lastHit)) {
                    continue;
                }

                if (entity.isRemoved()) {
                    return Target.NONE;
                }

                return Target.entities(entity);
            }

            return Target.NONE;
        }
    }
}
