package org.phantazm.server.config.lobby;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Namespaces;

import java.nio.file.Path;

public record LobbiesConfig(
    @NotNull Path instancesPath,
    @NotNull Component kickMessage,
    @NotNull Key mainLobby) {

    public static final Path DEFAULT_INSTANCES_PATH = Path.of("./lobbies/instances");

    public static final Component DEFAULT_KICK_MESSAGE =
        Component.text("Couldn't find where to send you!", NamedTextColor.RED);

    public static final Key DEFAULT_MAIN_LOBBY = Key.key(Namespaces.PHANTAZM, "main");

    public static final LobbiesConfig DEFAULT =
        new LobbiesConfig(DEFAULT_INSTANCES_PATH, DEFAULT_KICK_MESSAGE, DEFAULT_MAIN_LOBBY);
}
