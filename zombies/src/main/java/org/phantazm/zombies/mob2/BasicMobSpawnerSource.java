package org.phantazm.zombies.mob2;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.MobCreator;
import org.phantazm.mob2.MobSpawner;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class BasicMobSpawnerSource implements MobSpawnerSource {
    private final Supplier<? extends Map<Key, MobCreator>> mobCreatorMap;

    public BasicMobSpawnerSource(@NotNull Supplier<? extends Map<Key, MobCreator>> mobCreatorMap) {
        this.mobCreatorMap = Objects.requireNonNull(mobCreatorMap);
    }

    @Override
    public @NotNull MobSpawner make(@NotNull Supplier<ZombiesScene> scene) {
        return new ZombiesMobSpawner(mobCreatorMap.get(), scene);
    }
}
