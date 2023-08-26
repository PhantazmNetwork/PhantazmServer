package org.phantazm.mob2.selector;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;

@Model("mob.selector.last_hit")
@Cache
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

            return self.lastHitEntity().map(Target::entities).orElse(Target.NONE);
        }
    }
}
