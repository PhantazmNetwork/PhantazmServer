package org.phantazm.mob.validator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Model("mob.target_validator.line_of_sight")
@Cache
public class LineOfSightValidator implements TargetValidator {
    @FactoryMethod
    public LineOfSightValidator() {
    }

    @Override
    public boolean valid(@Nullable Entity targeter, @NotNull Entity entity) {
        if (targeter == null) {
            return false;
        }

        return targeter.hasLineOfSight(entity);
    }
}
