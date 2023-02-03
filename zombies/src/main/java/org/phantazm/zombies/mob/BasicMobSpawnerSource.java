package org.phantazm.zombies.mob;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import org.jetbrains.annotations.NotNull;
import org.phantazm.proxima.bindings.minestom.Spawner;
import org.phantazm.zombies.map.objects.MapObjects;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class BasicMobSpawnerSource implements MobSpawnerSource {
    private final Map<BooleanObjectPair<String>, ConfigProcessor<?>> processorMap;
    private final Spawner proximaSpawner;
    private final ContextManager contextManager;
    private final KeyParser keyParser;

    public BasicMobSpawnerSource(@NotNull Map<BooleanObjectPair<String>, ConfigProcessor<?>> processorMap,
            @NotNull Spawner proximaSpawner, @NotNull ContextManager contextManager, @NotNull KeyParser keyParser) {
        this.processorMap = Objects.requireNonNull(processorMap, "processorMap");
        this.proximaSpawner = Objects.requireNonNull(proximaSpawner, "proximaSpawner");
        this.contextManager = Objects.requireNonNull(contextManager, "contextManager");
        this.keyParser = Objects.requireNonNull(keyParser, "keyParser");
    }

    @Override
    public @NotNull BasicMobSpawner make(@NotNull Supplier<? extends MapObjects> mapObjects) {
        return new BasicMobSpawner(processorMap, proximaSpawner, contextManager, keyParser, mapObjects);
    }
}
