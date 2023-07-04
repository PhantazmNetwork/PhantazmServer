package org.phantazm.mob.validator;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Model("mob.target_validator.logic.or")
@Cache(false)
public class OrValidator implements TargetValidator {
    private final List<TargetValidator> validators;

    @FactoryMethod
    public OrValidator(@NotNull @Child("validators") List<TargetValidator> validators) {
        this.validators = validators;
    }

    @Override
    public boolean valid(@Nullable Entity targeter, @NotNull Entity entity) {
        for (TargetValidator validator : validators) {
            if (validator.valid(targeter, entity)) {
                return true;
            }
        }
        return false;
    }

    @DataObject
    public record Data(@NotNull @ChildPath("validators") List<String> validators) {
    }
}
