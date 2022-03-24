package com.github.phantazmnetwork.server.config.lobby;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

/**
 * Config for the server's {@link com.github.phantazmnetwork.api.game.scene.lobby.Lobby}s.
 */
public record LobbiesConfig(@NotNull Path instancesPath,
                            @NotNull Component kickMessage,
                            @NotNull String mainLobbyName,
                            @NotNull Map<String, LobbyConfig> lobbies) {

    /**
     * The default instances path.
     */
    public static final Path DEFAULT_INSTANCES_PATH = Path.of("./lobbies/");

    /**
     * The default kick message.
     */
    public static final Component DEFAULT_KICK_MESSAGE
            = Component.text("Couldn't find where to send you!", NamedTextColor.RED);

    /**
     * The default main lobby name.
     */
    public static final String DEFAULT_MAIN_LOBBY_NAME = "main";

    /**
     * The default LobbiesConfig instance.
     */
    public static final LobbiesConfig DEFAULT = new LobbiesConfig(DEFAULT_INSTANCES_PATH, DEFAULT_KICK_MESSAGE,
            DEFAULT_MAIN_LOBBY_NAME, Collections.emptyMap());

}
