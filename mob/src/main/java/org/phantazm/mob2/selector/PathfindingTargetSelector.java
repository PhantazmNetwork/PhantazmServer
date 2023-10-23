package org.phantazm.mob2.selector;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;

@Model("mob.selector.pathfinding_target")
@Cache
public class PathfindingTargetSelector implements SelectorComponent {
    @FactoryMethod
    public PathfindingTargetSelector() {

    }

    @Override
    public @NotNull Selector apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new PathfindingSelector(mob);
    }

    private record PathfindingSelector(Mob self) implements Selector {
        @Override
        public @NotNull Target select() {
            return Target.ofNullable(self.getTargetEntity());
        }
    }
}
