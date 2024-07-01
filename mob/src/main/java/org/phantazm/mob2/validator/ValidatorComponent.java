package org.phantazm.mob2.validator;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface ValidatorComponent extends Supplier<@NotNull Validator> {
}
