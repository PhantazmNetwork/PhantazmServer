package org.phantazm.zombies.mob;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.spawner.MobSpawner;
import org.phantazm.proxima.bindings.minestom.Spawner;
import org.phantazm.zombies.map.objects.MapObjects;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class BasicMobSpawnerSource implements MobSpawnerSource {
    private final Map<BooleanObjectPair<String>, ConfigProcessor<?>> processorMap;
    private final Spawner proximaSpawner;
    private final KeyParser keyParser;

    public BasicMobSpawnerSource(@NotNull Map<BooleanObjectPair<String>, ConfigProcessor<?>> processorMap,
            @NotNull Spawner proximaSpawner, @NotNull KeyParser keyParser) {
        this.processorMap = Objects.requireNonNull(processorMap, "processorMap");
        this.proximaSpawner = Objects.requireNonNull(proximaSpawner, "proximaSpawner");
        this.keyParser = Objects.requireNonNull(keyParser, "keyParser");
    }

    @Override
    public @NotNull MobSpawner make(@NotNull Supplier<? extends MapObjects> mapObjects, @NotNull MobStore mobStore) {


        return new BasicMobSpawner(processorMap, proximaSpawner, keyParser, mapObjects, mobStore);
    }
}
