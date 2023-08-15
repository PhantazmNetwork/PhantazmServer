package org.phantazm.mob2.selector;

import com.github.steanky.element.core.annotation.FactoryMethod;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Keys;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;

public class PathfindingTargetSelector implements SelectorComponent {
    @FactoryMethod
    public PathfindingTargetSelector() {

    }

    @Override
    public @NotNull Selector apply(@NotNull InjectionStore injectionStore) {
        return new PathfindingSelector(injectionStore.get(Keys.MOB_KEY));
    }

    private record PathfindingSelector(Mob self) implements Selector {
        @Override
        public @NotNull Target select() {
            return Target.ofNullable(self.getTargetEntity());
        }
    }
}
