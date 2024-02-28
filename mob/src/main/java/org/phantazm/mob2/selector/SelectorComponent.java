package org.phantazm.mob2.selector;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ExtensionHolder;

import java.util.function.Function;

public interface SelectorComponent extends Function<@NotNull ExtensionHolder, @NotNull Selector> {
}
