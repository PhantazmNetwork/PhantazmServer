package org.phantazm.mob2.validator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ExtensionHolder;

@Model("mob.validator.always")
@Cache
public class AlwaysValid implements ValidatorComponent {
    private static final Validator INSTANCE = (mob, entity) -> true;

    @FactoryMethod
    public AlwaysValid() {
    }

    @Override
    public @NotNull Validator apply(@NotNull ExtensionHolder holder) {
        return INSTANCE;
    }
}
