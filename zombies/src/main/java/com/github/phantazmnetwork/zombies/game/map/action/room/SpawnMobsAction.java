package com.github.phantazmnetwork.zombies.game.map.action.room;

import com.github.phantazmnetwork.zombies.game.map.Room;
import com.github.phantazmnetwork.zombies.game.map.Round;
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.game.map.action.Action;
import com.github.phantazmnetwork.zombies.map.MapProcessors;
import com.github.phantazmnetwork.zombies.map.SpawnInfo;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@ElementModel("zombies.map.room.action.spawn_mobs")
public class SpawnMobsAction implements Action<Room> {
    private static final ConfigProcessor<Data> PROCESSOR = new ConfigProcessor<>() {
        private static final ConfigProcessor<List<SpawnInfo>> SPAWN_INFO_LIST_PROCESSOR =
                MapProcessors.spawnInfo().listProcessor();

        @Override
        public @NotNull Data dataFromElement(@NotNull ConfigElement node) throws ConfigProcessException {
            List<SpawnInfo> spawns = SPAWN_INFO_LIST_PROCESSOR.dataFromElement(node.getElementOrThrow("mobSpawns"));
            int priority = node.getNumberOrThrow("priority").intValue();
            return new Data(spawns, priority);
        }

        @Override
        public @NotNull ConfigNode elementFromData(@NotNull Data data) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(2);
            node.put("mobSpawns", SPAWN_INFO_LIST_PROCESSOR.elementFromData(data.mobSpawns));
            node.putNumber("priority", data.priority);
            return node;
        }
    };
    private final Data data;
    private final Supplier<? extends Round> currentRound;

    @FactoryMethod
    public SpawnMobsAction(@NotNull Data data, @NotNull @ElementDependency("zombies.dependency.map") ZombiesMap map) {
        this.data = Objects.requireNonNull(data, "data");
        this.currentRound = map::currentRound;
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return PROCESSOR;
    }

    @Override
    public void perform(@NotNull Room room) {
        Round current = currentRound.get();
        if (current != null) {
            current.spawnMobs(data.mobSpawns);
        }
    }

    @Override
    public int priority() {
        return data.priority;
    }

    @ElementData
    public record Data(@NotNull List<SpawnInfo> mobSpawns, int priority) {
    }
}
