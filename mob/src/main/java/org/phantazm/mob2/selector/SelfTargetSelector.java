package org.phantazm.mob2.selector;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;

@Model("mob.selector.self")
@Cache
public class SelfTargetSelector implements SelectorComponent {
    @FactoryMethod
    public SelfTargetSelector() {
    }

    @Override
    public @NotNull Selector apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new SelfSelector(Target.entities(mob));
    }

    private record SelfSelector(Target target) implements Selector {
        @Override
        public @NotNull Target select() {
            return target;
        }
    }
}
