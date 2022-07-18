package com.github.phantazmnetwork.zombies.game.map.action.room;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.component.KeyedConfigProcessor;
import com.github.phantazmnetwork.commons.component.annotation.ComponentData;
import com.github.phantazmnetwork.commons.component.annotation.ComponentFactory;
import com.github.phantazmnetwork.commons.component.annotation.ComponentModel;
import com.github.phantazmnetwork.commons.component.annotation.ComponentProcessor;
import com.github.phantazmnetwork.zombies.game.map.Room;
import com.github.phantazmnetwork.zombies.game.map.Round;
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.game.map.action.Action;
import com.github.phantazmnetwork.zombies.map.MapProcessors;
import com.github.phantazmnetwork.zombies.map.SpawnInfo;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@ComponentModel("phantazm:zombies.map.room.action.spawn_mobs")
public class SpawnMobsAction implements Action<Room> {
    private static final KeyedConfigProcessor<Data> PROCESSOR = new KeyedConfigProcessor<>() {
        @Override
        public @NotNull Data dataFromNode(@NotNull ConfigNode node) throws ConfigProcessException {
            List<SpawnInfo> spawns =
                    MapProcessors.spawnInfo().listProcessor().dataFromElement(node.getElementOrThrow("mobSpawns"));
            int priority = node.getNumberOrThrow("priority").intValue();
            return new Data(spawns, priority);
        }

        @Override
        public @NotNull ConfigNode nodeFromData(Data data) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(2);
            node.put("mobSpawns", MapProcessors.spawnInfo().listProcessor().elementFromData(data.mobSpawns));
            node.putNumber("priority", data.priority);
            return node;
        }
    };

    @ComponentProcessor
    public static @NotNull KeyedConfigProcessor<Data> processor() {
        return PROCESSOR;
    }

    private final Data data;
    private final Supplier<? extends Round> currentRound;

    @ComponentFactory
    public SpawnMobsAction(@NotNull Data data, @NotNull ZombiesMap.ObjectContext context) {
        this.data = Objects.requireNonNull(data, "data");
        this.currentRound = Objects.requireNonNull(context.currentRoundSupplier(), "currentRound");
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

    @ComponentData
    public record Data(@NotNull List<SpawnInfo> mobSpawns, int priority) implements Keyed {
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "zombies.map.room.action.spawn_mobs");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }
}
