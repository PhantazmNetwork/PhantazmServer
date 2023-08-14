package org.phantazm.mob2.validator;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;

public class AlwaysValid implements ValidatorComponent {
    private static final Validator INSTANCE = entity -> true;

    @Override
    public @NotNull Validator apply(@NotNull InjectionStore injectionStore) {
        return INSTANCE;
    }
}
