package com.github.phantazmnetwork.zombies.stage;

import com.github.phantazmnetwork.commons.Activable;

public interface Stage extends Activable {

    boolean shouldEnd();

    boolean hasPermanentPlayers();

}
