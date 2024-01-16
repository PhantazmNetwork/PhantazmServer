package org.phantazm.zombies.mob2;

import org.jetbrains.annotations.NotNull;
import org.phantazm.loader.Loader;
import org.phantazm.mob2.MobCreator;
import org.phantazm.mob2.MobSpawner;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Objects;
import java.util.function.Supplier;

public class BasicMobSpawnerSource implements MobSpawnerSource {
    private final Loader<MobCreator> mobCreatorLoader;

    public BasicMobSpawnerSource(@NotNull Loader<MobCreator> mobCreatorLoader) {
        this.mobCreatorLoader = Objects.requireNonNull(mobCreatorLoader);
    }

    @Override
    public @NotNull MobSpawner make(@NotNull Supplier<ZombiesScene> scene) {
        return new ZombiesMobSpawner(mobCreatorLoader, scene);
    }
}
