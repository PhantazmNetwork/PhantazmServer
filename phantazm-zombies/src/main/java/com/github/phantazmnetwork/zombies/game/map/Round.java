package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.zombies.map.RoundInfo;
import com.github.phantazmnetwork.zombies.map.WaveInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class Round extends MapObject<RoundInfo> implements Tickable {
    private final Logger LOGGER = LoggerFactory.getLogger(Round.class);

    private final List<Wave> unmodifiableWaves;
    private final Consumer<Round> action;

    private boolean isActive;

    /**
     * Constructs a new instance of this class.
     *
     * @param roundInfo the backing data object
     */
    public Round(@NotNull RoundInfo roundInfo, @NotNull Consumer<Round> action) {
        super(roundInfo);
        List<WaveInfo> waveInfo = roundInfo.waves();
        if(waveInfo.size() == 0) {
            LOGGER.warn("Round {} has no waves", roundInfo.round());
        }

        List<Wave> waves = new ArrayList<>(waveInfo.size());
        for(WaveInfo info : waveInfo) {
            waves.add(new Wave(info));
        }

        this.unmodifiableWaves = Collections.unmodifiableList(waves);
        this.action = Objects.requireNonNull(action, "action");
    }

    public @UnmodifiableView @NotNull List<Wave> getWaves() {
        return unmodifiableWaves;
    }

    public boolean isActive() {
        return isActive;
    }

    public void spawnRound() {

    }

    public void endRound() {

    }

    @Override
    public void tick(long time) {

    }
}
