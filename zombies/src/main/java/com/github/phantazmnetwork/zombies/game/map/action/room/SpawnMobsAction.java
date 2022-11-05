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
import java.util.function.Supplier;

@Model("zombies.map.room.action.spawn_mobs")
public class SpawnMobsAction implements Action<Room> {
    private final Data data;
    private final Supplier<? extends RoundHandler> roundHandlerSupplier;

    @FactoryMethod
    public SpawnMobsAction(@NotNull Data data,
            @NotNull @Dependency("zombies.dependency.map_object.round_handler_supplier")
            Supplier<? extends RoundHandler> roundHandlerSupplier) {
        this.data = Objects.requireNonNull(data, "data");
        this.roundHandlerSupplier = Objects.requireNonNull(roundHandlerSupplier, "roundHandlerSupplier");
    }

    @Override
    public void perform(@NotNull Room room) {
        Round current = roundHandlerSupplier.get().currentRound();
        if (current != null) {
            current.spawnMobs(data.mobSpawns);
        }
    }

    @DataObject
    public record Data(@NotNull List<SpawnInfo> mobSpawns) {
    }
}
