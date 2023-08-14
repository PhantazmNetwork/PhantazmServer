package org.phantazm.mob2.selector;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;

import java.util.function.Function;

public interface SelectorComponent extends Function<@NotNull InjectionStore, @NotNull Selector> {
}
