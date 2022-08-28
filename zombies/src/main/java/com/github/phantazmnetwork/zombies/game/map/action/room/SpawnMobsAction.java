package com.github.phantazmnetwork.zombies.game.map.action.room;

import com.github.phantazmnetwork.zombies.game.map.Room;
import com.github.phantazmnetwork.zombies.game.map.Round;
import com.github.phantazmnetwork.zombies.game.map.RoundHandler;
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

@Model("zombies.map.room.action.spawn_mobs")
public class SpawnMobsAction implements Action<Room> {
    private final Data data;
    private final RoundHandler roundHandler;

    @FactoryMethod
    public SpawnMobsAction(@NotNull Data data,
            @NotNull @Dependency("zombies.dependency.map_object.round_handler") RoundHandler roundHandler) {
        this.data = Objects.requireNonNull(data, "data");
        this.roundHandler = Objects.requireNonNull(roundHandler, "roundHandler");
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            private static final ConfigProcessor<List<SpawnInfo>> SPAWN_INFO_LIST_PROCESSOR =
                    MapProcessors.spawnInfo().listProcessor();

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement node) throws ConfigProcessException {
                List<SpawnInfo> mobSpawns =
                        SPAWN_INFO_LIST_PROCESSOR.dataFromElement(node.getElementOrThrow("mobSpawns"));
                return new Data(mobSpawns);
            }

            @Override
            public @NotNull ConfigNode elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(2);
                node.put("mobSpawns", SPAWN_INFO_LIST_PROCESSOR.elementFromData(data.mobSpawns));
                return node;
            }
        };
    }

    @Override
    public void perform(@NotNull Room room) {
        Round current = roundHandler.currentRound();
        if (current != null) {
            current.spawnMobs(data.mobSpawns);
        }
    }

    @DataObject
    public record Data(@NotNull List<SpawnInfo> mobSpawns) {
    }
}
