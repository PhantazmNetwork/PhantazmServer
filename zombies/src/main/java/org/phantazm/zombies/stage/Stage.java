package org.phantazm.zombies.stage;

import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Activable;
import org.phantazm.zombies.player.ZombiesPlayer;

public interface Stage extends Activable, Keyed {

    boolean shouldContinue();

    boolean shouldRevert();

    void onJoin(@NotNull ZombiesPlayer zombiesPlayer);

    void onLeave(@NotNull ZombiesPlayer zombiesPlayer);

    boolean hasPermanentPlayers();

}
