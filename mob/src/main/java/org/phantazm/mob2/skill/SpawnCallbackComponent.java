package org.phantazm.mob2.skill;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ExtensionHolder;

import java.util.function.Function;

public interface SpawnCallbackComponent extends Function<@NotNull ExtensionHolder, @NotNull SpawnCallback> {

}