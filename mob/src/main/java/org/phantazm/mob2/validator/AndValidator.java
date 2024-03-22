package org.phantazm.mob2.validator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.Mob;

import java.util.ArrayList;
import java.util.List;

@Model("mob.validator.and")
@Cache
public class AndValidator implements ValidatorComponent {
    private final List<ValidatorComponent> validators;

    @FactoryMethod
    public AndValidator(@NotNull @Child("validators") List<ValidatorComponent> validators) {
        this.validators = validators;
    }

    @Override
    public @NotNull Validator apply(@NotNull ExtensionHolder holder) {
        List<Validator> validators = new ArrayList<>(this.validators.size());
        for (ValidatorComponent component : this.validators) {
            validators.add(component.apply(holder));
        }

        return new Internal(validators);
    }

    private record Internal(List<Validator> validators) implements Validator {
        @Override
        public boolean valid(@NotNull Mob mob, @NotNull Entity entity) {
            for (Validator validator : validators) {
                if (!validator.valid(mob, entity)) {
                    return false;
                }
            }

            return true;
        }
    }
}
