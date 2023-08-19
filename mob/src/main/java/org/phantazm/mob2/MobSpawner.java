package org.phantazm.mob2;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface MobSpawner {
    default @NotNull Mob spawn(@NotNull Key identifier, @NotNull Instance instance, @NotNull Pos pos) {
        return spawn(identifier, instance, pos, ignored -> {
        });
    }

    @NotNull Mob spawn(@NotNull Key identifier, @NotNull Instance instance, @NotNull Pos pos,
        @NotNull Consumer<? super Mob> setup);
}
