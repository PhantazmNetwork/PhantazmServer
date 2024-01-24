package org.phantazm.zombies.npc;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.loader.Loader;
import org.phantazm.zombies.modifier.ModifierHandler;

public final class InjectionKeys {
    public record ModifierHandlerLoader(@NotNull Loader<ModifierHandler> modifierHandlerLoader) {
    }

    public static final InjectionStore.Key<ModifierHandlerLoader> MODIFIER_LOADER_KEY =
        InjectionStore.key(ModifierHandlerLoader.class);
}
