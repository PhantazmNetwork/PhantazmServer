package org.phantazm.mob2.validator;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;

import java.util.function.Function;

public interface ValidatorComponent extends Function<@NotNull InjectionStore, @NotNull Validator> {
}
