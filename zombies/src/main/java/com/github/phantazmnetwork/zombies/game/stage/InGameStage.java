package com.github.phantazmnetwork.zombies.game.stage;

import com.github.phantazmnetwork.commons.Activable;
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class InGameStage extends StageBase {

    private final ZombiesMap map;

    private long ticksSinceStart = 0L;

    public InGameStage(@NotNull Collection<Activable> activables, @NotNull ZombiesMap map) {
        super(activables);
        this.map = Objects.requireNonNull(map, "map");
    }

    @Override
    public void tick(long time) {
        super.tick(time);
        map.tick(time);
        ticksSinceStart++;
    }

    @Override
    public void start() {
        super.start();
        map.startRound(0);
        ticksSinceStart = 0L;
    }

    @Override
    public void end() {
        ticksSinceStart = -1L;
    }

    @Override
    public boolean shouldEnd() {
        return map.currentRound() != null;
    }

    @Override
    public boolean hasPermanentPlayers() {
        return true;
    }

    public long getTicksSinceStart() {
        return ticksSinceStart;
    }
}
