package com.github.phantazmnetwork.zombies.game.stage;

import com.github.phantazmnetwork.commons.Tickable;

public interface Stage extends Tickable {

    void start();

    void end();

    boolean shouldEnd();

    boolean hasPermanentPlayers();

}
