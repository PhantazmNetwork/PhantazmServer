package org.phantazm.mob2.selector;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;

import java.util.function.BiFunction;

public interface SelectorComponent extends BiFunction<@NotNull Mob, @NotNull InjectionStore, @NotNull Selector> {
}
