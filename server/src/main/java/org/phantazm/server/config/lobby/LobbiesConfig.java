package org.phantazm.server.config.lobby;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Namespaces;

public record LobbiesConfig(@NotNull Key mainLobby) {
    public static final Key DEFAULT_MAIN_LOBBY = Key.key(Namespaces.PHANTAZM, "main");

    public static final LobbiesConfig DEFAULT =
        new LobbiesConfig(DEFAULT_MAIN_LOBBY);
}
