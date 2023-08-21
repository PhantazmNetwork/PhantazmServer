package org.phantazm.mob2;

import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;

/**
 * A source of like-behavior {@link Mob} objects.
 */
public interface MobCreator {
    @NotNull Mob create(@NotNull Instance instance, @NotNull InjectionStore injectionStore);
}
