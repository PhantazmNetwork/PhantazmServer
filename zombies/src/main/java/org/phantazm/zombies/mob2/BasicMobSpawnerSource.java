package org.phantazm.zombies.mob2;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.loader.Loader;
import org.phantazm.mob2.MobCreator;
import org.phantazm.mob2.MobSpawner;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class BasicMobSpawnerSource implements MobSpawnerSource {
    private final Loader<MobCreator> mobCreatorLoader;
    private final Supplier<? extends Map<Key, ExtensionHolder>> extensionMapSupplier;

    public BasicMobSpawnerSource(@NotNull Loader<MobCreator> mobCreatorLoader,
        @NotNull Supplier<? extends Map<Key, ExtensionHolder>> extensionMapSupplier) {
        this.mobCreatorLoader = Objects.requireNonNull(mobCreatorLoader);
        this.extensionMapSupplier = extensionMapSupplier;
    }

    @Override
    public @NotNull MobSpawner make(@NotNull Supplier<ZombiesScene> scene) {
        return new ZombiesMobSpawner(mobCreatorLoader, extensionMapSupplier.get(), scene);
    }
}
