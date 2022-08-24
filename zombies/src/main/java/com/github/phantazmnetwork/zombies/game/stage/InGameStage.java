package com.github.phantazmnetwork.zombies.game.stage;

import com.github.phantazmnetwork.commons.Activable;
import com.github.phantazmnetwork.commons.Wrapper;
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class InGameStage extends StageBase {

    private final ZombiesMap map;

    private final Wrapper<Long> ticksSinceStart;

    public InGameStage(@NotNull Collection<Activable> activables, @NotNull ZombiesMap map,
            @NotNull Wrapper<Long> ticksSinceStart) {
        super(activables);
        this.map = Objects.requireNonNull(map, "map");
        this.ticksSinceStart = Objects.requireNonNull(ticksSinceStart, "ticksSinceStart");
    }

    @Override
    public void tick(long time) {
        super.tick(time);
        map.tick(time);
        ticksSinceStart.apply(ticks -> ticks + 1);
    }

    @Override
    public void start() {
        super.start();
        if (!map.getRounds().isEmpty()) {
            map.startRound(0);
        }
        ticksSinceStart.set(0L);
    }

    @Override
    public boolean shouldEnd() {
        return map.currentRound() == null;
    }

    @Override
    public boolean hasPermanentPlayers() {
        return true;
    }

}
