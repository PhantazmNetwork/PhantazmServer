package org.phantazm.mob2.selector;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;

public class SelfTargetSelector implements SelectorComponent {
    @Override
    public @NotNull Selector apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new SelfSelector(mob);
    }

    private record SelfSelector(Mob self) implements Selector {
        @Override
        public @NotNull Target select() {
            return Target.entities(self);
        }
    }
}
