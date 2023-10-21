package org.phantazm.zombies;

import net.kyori.adventure.key.Key;
import org.phantazm.commons.Namespaces;

public final class Stages {
    private Stages() {
        throw new UnsupportedOperationException();
    }

    public static final Key ZOMBIES_GAME = Key.key(Namespaces.PHANTAZM, "zombies_game");
}
