package org.phantazm.server.config.lobby;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.lobby.Lobby;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Config for the server's {@link Lobby}s.
 *
 * @param instancesPath The path where {@link Instance}s are located
 * @param kickMessage   A {@link Component} used to display a message to the player when they are kicked when no valid lobby exists to route them to
 * @param mainLobbyName The {@link String} name of the main lobby
 * @param lobbies       A {@link Map} of {@link String} lobby names to their respective {@link LobbyConfig}s
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
    public static final Component DEFAULT_KICK_MESSAGE =
            Component.text("Couldn't find where to send you!", NamedTextColor.RED);

    /**
     * The default main lobby name.
     */
    public static final String DEFAULT_MAIN_LOBBY_NAME = "main";

    /**
     * The default LobbiesConfig instance.
     */
    public static final LobbiesConfig DEFAULT =
            new LobbiesConfig(DEFAULT_INSTANCES_PATH, DEFAULT_KICK_MESSAGE, DEFAULT_MAIN_LOBBY_NAME,
                    Collections.emptyMap());

    /**
     * Creates a {@link LobbiesConfig}.
     *
     * @param instancesPath The path where {@link Instance}s are located
     * @param kickMessage   A {@link Component} used to display a message to the player when they are kicked when no valid lobby exists to route them to
     * @param mainLobbyName The {@link String} name of the main lobby
     * @param lobbies       A {@link Map} of {@link String} lobby names to their respective {@link LobbyConfig}s
     */
    public LobbiesConfig {
        Objects.requireNonNull(instancesPath, "instancesPath");
        Objects.requireNonNull(kickMessage, "kickMessage");
        Objects.requireNonNull(mainLobbyName, "mainLobbyName");
        Objects.requireNonNull(lobbies, "lobbies");
        for (LobbyConfig config : lobbies.values()) {
            Objects.requireNonNull(config, "lobbies config");
        }
    }

}
