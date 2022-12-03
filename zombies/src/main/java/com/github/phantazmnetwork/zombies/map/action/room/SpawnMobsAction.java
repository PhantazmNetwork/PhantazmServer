package com.github.phantazmnetwork.zombies.map.action.room;

import com.github.phantazmnetwork.zombies.map.Room;
import com.github.phantazmnetwork.zombies.map.RoundHandler;
import com.github.phantazmnetwork.zombies.map.SpawnInfo;
import com.github.phantazmnetwork.zombies.map.action.Action;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
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
        roundHandlerSupplier.get().currentRound().ifPresent(round -> round.spawnMobs(data.mobSpawns));
    }

    @DataObject
    public record Data(@NotNull List<SpawnInfo> mobSpawns) {
    }
}
