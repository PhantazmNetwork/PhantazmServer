package com.github.phantazmnetwork.zombies.game.scoreboard.score;

import com.github.phantazmnetwork.commons.Tickable;

public interface ScoreboardUpdater extends Tickable {

    void invalidateCache();

}
