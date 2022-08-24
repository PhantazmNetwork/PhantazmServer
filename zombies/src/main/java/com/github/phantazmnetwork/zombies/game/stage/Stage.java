package com.github.phantazmnetwork.zombies.game.stage;

import com.github.phantazmnetwork.commons.Activable;

public interface Stage extends Activable {

    boolean shouldEnd();

    boolean hasPermanentPlayers();

}
