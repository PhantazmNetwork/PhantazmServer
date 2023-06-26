package org.phantazm.zombies.stage;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Activable;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public interface Stage extends Activable, Keyed {

    boolean shouldContinue();

    boolean shouldRevert();

    void onJoin(@NotNull ZombiesPlayer zombiesPlayer);

    void onLeave(@NotNull ZombiesPlayer zombiesPlayer);

    boolean hasPermanentPlayers();

}
