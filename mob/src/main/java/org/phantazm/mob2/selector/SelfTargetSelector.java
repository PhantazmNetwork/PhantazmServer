package org.phantazm.mob2.selector;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;

@Model("mob.selector.self")
@Cache
public class SelfTargetSelector implements SelectorComponent {
    @FactoryMethod
    public SelfTargetSelector() {
    }

    @Override
    public @NotNull Selector apply(@NotNull ExtensionHolder holder) {
        return new SelfSelector();
    }

    private record SelfSelector() implements Selector {
        @Override
        public @NotNull Target select(@NotNull Mob mob) {
            return Target.entities(mob);
        }
    }
}
