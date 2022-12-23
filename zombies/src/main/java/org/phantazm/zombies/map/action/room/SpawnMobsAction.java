package org.phantazm.zombies.map.action.room;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Room;
import org.phantazm.zombies.map.Round;
import org.phantazm.zombies.map.SpawnInfo;
import org.phantazm.zombies.map.action.Action;
import org.phantazm.zombies.map.handler.RoundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@Model("zombies.map.room.action.spawn_mobs")
public class SpawnMobsAction implements Action<Room> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpawnMobsAction.class);

    private final Data data;
    private final Supplier<? extends RoundHandler> roundHandlerSupplier;

    @FactoryMethod
    public SpawnMobsAction(@NotNull Data data,
            @NotNull @Dependency Supplier<? extends RoundHandler> roundHandlerSupplier) {
        this.data = Objects.requireNonNull(data, "data");
        this.roundHandlerSupplier = Objects.requireNonNull(roundHandlerSupplier, "roundHandlerSupplier");
    }

    @Override
    public void perform(@NotNull Room room) {
        Optional<Round> currentRound = roundHandlerSupplier.get().currentRound();
        if (currentRound.isPresent()) {
            currentRound.get().spawnMobs(data.mobSpawns);
        }
        else {
            LOGGER.warn("Tried to spawn {} mobs but there is no active round", data.mobSpawns.size());
        }
    }

    @DataObject
    public record Data(@NotNull List<SpawnInfo> mobSpawns) {
    }
}
