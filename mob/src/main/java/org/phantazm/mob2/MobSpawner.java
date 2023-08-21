package org.phantazm.mob2;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;

import java.util.function.Consumer;

public interface MobSpawner {
    @NotNull Mob spawn(@NotNull Key identifier, @NotNull Instance instance, @NotNull Pos pos,
        @NotNull Consumer<? super @NotNull Mob> setup);

    default @NotNull Mob spawn(@NotNull Key identifier, @NotNull Instance instance, @NotNull Pos pos) {
        return spawn(identifier, instance, pos, ignored -> {
        });
    }

    /**
     * Initializes this MobSpawner, preparing any necessary {@link InjectionStore} entries. This method will typically
     * call {@link MobSpawner#buildDependencies(InjectionStore.Builder)}.
     */
    void init();

    /**
     * Called by the MobSpawner to prepare any necessary {@link InjectionStore} entries.
     *
     * @param builder the builder to append dependencies to
     */
    void buildDependencies(InjectionStore.@NotNull Builder builder);
}
