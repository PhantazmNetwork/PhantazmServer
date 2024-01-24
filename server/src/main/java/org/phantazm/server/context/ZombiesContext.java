package org.phantazm.server.context;

import org.jetbrains.annotations.NotNull;
import org.phantazm.loader.Loader;
import org.phantazm.zombies.modifier.ModifierHandler;

public record ZombiesContext(@NotNull Loader<ModifierHandler> modifierHandlerLoader) {
}
