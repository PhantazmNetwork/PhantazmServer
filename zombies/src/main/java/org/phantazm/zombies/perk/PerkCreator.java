package org.phantazm.zombies.perk;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

public interface PerkCreator {

    PerkLevel createPerk(@NotNull ZombiesPlayer user);

}
