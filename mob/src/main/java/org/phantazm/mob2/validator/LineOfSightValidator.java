package org.phantazm.mob2.validator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;

@Model("mob.validator.line_of_sight")
@Cache
public class LineOfSightValidator implements ValidatorComponent {
    @FactoryMethod
    public LineOfSightValidator() {
    }

    @Override
    public @NotNull Validator apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Internal(mob);
    }

    private record Internal(Mob self) implements Validator {
        @Override
        public boolean valid(@NotNull Entity entity) {
            return self.hasLineOfSight(entity);
        }
    }
}
