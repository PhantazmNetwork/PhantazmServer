package org.phantazm.mob2.validator;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;

import java.util.function.BiFunction;

public interface ValidatorComponent extends BiFunction<@NotNull Mob, @NotNull InjectionStore, @NotNull Validator> {
}
