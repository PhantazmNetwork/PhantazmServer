package org.phantazm.mob2;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ExtensionHolder;

import java.util.function.Consumer;

/**
 * Spawns {@link Mob}s in an instance.
 */
public interface MobSpawner {
    class Extensions {
        private static final ExtensionHolder GLOBAL_HOLDER = new ExtensionHolder();

        public static @NotNull ExtensionHolder newHolder() {
            return GLOBAL_HOLDER.derive();
        }

        public static <T> ExtensionHolder.@NotNull Key<T> newKey(@NotNull Class<T> type) {
            return GLOBAL_HOLDER.requestKey(type);
        }
    }

    /**
     * Spawns a {@link Mob} in the given instance. This method must be thread-safe.
     *
     * @param identifier the mob to spawn
     * @param instance   the instance to spawn the mob in
     * @param pos        the position to spawn the mob at
     * @param setup      a callback to run after the mob is created but before it is added to the instance
     * @return the spawned mob; its instance having been set to {@code instance}
     * @throws IllegalStateException    if this spawner has not been initialized yet
     * @throws IllegalArgumentException if no such mob exists with the given identifier
     */
    @NotNull
    Mob spawn(@NotNull Key identifier, @NotNull Instance instance, @NotNull Pos pos,
        @NotNull Consumer<? super @NotNull Mob> setup);

    /**
     * Convenience override for {@link MobSpawner#spawn(Key, Instance, Pos, Consumer)}, with an empty setup callback.
     *
     * @param identifier the mob to spawn
     * @param instance   the instance to spawn the mob in
     * @param pos        the position to spawn the mob at
     * @return the spawned mob; its instance having been set to {@code instance}
     * @throws IllegalStateException    if this spawner has not been initialized yet
     * @throws IllegalArgumentException if no such mob exists with the given identifier
     */
    default @NotNull Mob spawn(@NotNull Key identifier, @NotNull Instance instance, @NotNull Pos pos) {
        return spawn(identifier, instance, pos, ignored -> {
        });
    }

    /**
     * Determines if this spawner can spawn a mob with the given identifier.
     *
     * @param identifier the identifier to check
     * @return true if this spawner can spawn the mob; false otherwise
     */
    boolean canSpawn(@NotNull Key identifier);
}
