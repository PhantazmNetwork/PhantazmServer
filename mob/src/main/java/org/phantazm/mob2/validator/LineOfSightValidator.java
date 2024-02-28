package org.phantazm.mob2.validator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.Mob;

@Model("mob.validator.line_of_sight")
@Cache
public class LineOfSightValidator implements ValidatorComponent {
    @FactoryMethod
    public LineOfSightValidator() {
    }

    @Override
    public @NotNull Validator apply(@NotNull ExtensionHolder holder) {
        return new Internal();
    }

    private record Internal() implements Validator {
        @Override
        public boolean valid(@NotNull Mob mob, @NotNull Entity entity) {
            return mob.hasLineOfSight(entity);
        }
    }
}
