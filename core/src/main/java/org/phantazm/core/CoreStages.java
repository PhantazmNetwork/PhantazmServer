package org.phantazm.core;

import net.kyori.adventure.key.Key;
import org.phantazm.commons.Namespaces;

public final class CoreStages {
    private CoreStages() {
        throw new UnsupportedOperationException();
    }

    public static final Key LOBBY = Key.key(Namespaces.PHANTAZM, "lobby");
}
