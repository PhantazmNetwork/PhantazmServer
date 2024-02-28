package org.phantazm.mob2.selector;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;

@Model("mob.selector.last_hit")
@Cache
public class LastHitSelector implements SelectorComponent {
    @FactoryMethod
    public LastHitSelector() {
    }

    @Override
    public @NotNull Selector apply(@NotNull ExtensionHolder holder) {
        return new Internal();
    }

    private record Internal() implements Selector {
        @Override
        public @NotNull Target select(@NotNull Mob mob) {
            return mob.lastHitEntity().map(Target::entities).orElse(Target.NONE);
        }
    }
}
