package org.phantazm.mob2.validator;

import com.github.steanky.element.core.annotation.FactoryMethod;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;

public class AlwaysValid implements ValidatorComponent {
    private static final Validator INSTANCE = entity -> true;

    @FactoryMethod
    public AlwaysValid() {
    }

    @Override
    public @NotNull Validator apply(@NotNull InjectionStore injectionStore) {
        return INSTANCE;
    }
}
