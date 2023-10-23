package org.phantazm.zombies.mob2;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.MobSpawner;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.function.Supplier;

public interface MobSpawnerSource {
    @NotNull MobSpawner make(@NotNull Supplier<ZombiesScene> scene);
}
