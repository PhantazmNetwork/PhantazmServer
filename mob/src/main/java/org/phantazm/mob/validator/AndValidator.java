package org.phantazm.mob.validator;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Model("mob.target_validator.logic.and")
@Cache(false)
public class AndValidator implements TargetValidator {
    private final List<TargetValidator> validators;

    @FactoryMethod
    public AndValidator(@NotNull @Child("validators") List<TargetValidator> validators) {
        this.validators = validators;
    }

    @Override
    public boolean valid(@NotNull Entity entity) {
        for (TargetValidator validator : validators) {
            if (!validator.valid(entity)) {
                return false;
            }
        }

        return true;
    }

    @DataObject
    public record Data(@NotNull @ChildPath("validators") List<String> validators) {
    }
}
