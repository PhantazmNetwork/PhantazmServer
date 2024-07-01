package org.phantazm.zombies.player.upgrade;

import net.minestom.server.Tickable;
import org.phantazm.core.tick.Activable;

public interface PlayerUpgrade extends Tickable, Activable {
    boolean needsTicking();

    boolean isActivated();
}
