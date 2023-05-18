package org.phantazm.zombies.stage;

import net.kyori.adventure.key.Key;
import org.phantazm.commons.Namespaces;

public final class StageKeys {
    private StageKeys() {
    }

    public static final Key IDLE_STAGE = Key.key(Namespaces.PHANTAZM, "stage.idle");

    public static final Key COUNTDOWN = Key.key(Namespaces.PHANTAZM, "stage.countdown");

    public static final Key IN_GAME = Key.key(Namespaces.PHANTAZM, "stage.in_game");

    public static final Key END = Key.key(Namespaces.PHANTAZM, "stage.end");
}
