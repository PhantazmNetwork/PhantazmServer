package org.phantazm.server;

import com.github.steanky.ethylene.codec.toml.TomlCodec;
import com.github.steanky.ethylene.codec.yaml.YamlCodec;
import com.github.steanky.ethylene.core.BasicConfigHandler;
import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.ConfigHandler;
import com.github.steanky.ethylene.core.loader.SyncFileConfigLoader;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import com.github.steanky.ethylene.mapper.type.Token;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.chat.ChatConfig;
import org.phantazm.core.guild.party.PartyConfig;
import org.phantazm.server.command.whisper.WhisperConfig;
import org.phantazm.server.config.lobby.LobbiesConfig;
import org.phantazm.server.config.player.PlayerConfig;
import org.phantazm.server.config.server.*;
import org.phantazm.server.config.zombies.ZombiesConfig;
import org.phantazm.zombies.modifier.ModifierCommandConfig;

import java.nio.file.Path;

/**
 * Entrypoint for configuration-related features.
 */
public final class ConfigFeature {

    public static final Path PLAYER_CONFIG_PATH = Path.of("./player-config.toml");
    /**
     * The location of the server configuration file.
     */
    public static final Path SERVER_CONFIG_PATH = Path.of("./server-config.toml");
    /**
     * The location of the lobbies configuration file.
     */
    public static final Path LOBBIES_CONFIG_PATH = Path.of("./lobbies-config.yml");

    /**
     * The location of the pathfinder configuration file.
     */
    public static final Path PATHFINDER_CONFIG_PATH = Path.of("./pathfinder-config.toml");

    /**
     * The location of the shutdown configuration file.
     */
    public static final Path SHUTDOWN_CONFIG_PATH = Path.of("./shutdown-config.toml");

    public static final Path STARTUP_CONFIG_PATH = Path.of("./startup-config.toml");

    public static final Path PARTY_CONFIG_PATH = Path.of("./party-config.toml");

    public static final Path WHISPER_CONFIG_PATH = Path.of("./whisper-config.toml");

    public static final Path CHAT_CONFIG_PATH = Path.of("./chat-config.yml");

    public static final Path JOIN_REPORT_CONFIG_PATH = Path.of("./join-report-config.toml");

    public static final Path ZOMBIES_CONFIG_PATH = Path.of("./zombies-config.toml");

    public static final Path MODIFIER_COMMAND_CONFIG_PATH = Path.of("./modifier-config.toml");

    public static final ConfigHandler.ConfigKey<PlayerConfig> PLAYER_CONFIG_KEY = new ConfigHandler.ConfigKey<>(
        PlayerConfig.class, "player_config");

    /**
     * The {@link ConfigHandler.ConfigKey} instance used to refer to the primary {@link ServerConfig} loader.
     */
    public static final ConfigHandler.ConfigKey<ServerConfig> SERVER_CONFIG_KEY =
        new ConfigHandler.ConfigKey<>(ServerConfig.class, "server_config");
    /**
     * The {@link ConfigHandler.ConfigKey} instance used to refer to the primary {@link LobbiesConfig} loader.
     */
    public static final ConfigHandler.ConfigKey<LobbiesConfig> LOBBIES_CONFIG_KEY =
        new ConfigHandler.ConfigKey<>(LobbiesConfig.class, "lobbies_config");

    /**
     * The {@link ConfigHandler.ConfigKey} instance used to refer to the primary {@link PathfinderConfig} loader.
     */
    public static final ConfigHandler.ConfigKey<PathfinderConfig> PATHFINDER_CONFIG_KEY =
        new ConfigHandler.ConfigKey<>(PathfinderConfig.class, "pathfinder_config");

    /**
     * The {@link ConfigHandler.ConfigKey} instance used to refer to the primary {@link PathfinderConfig} loader.
     */
    public static final ConfigHandler.ConfigKey<ShutdownConfig> SHUTDOWN_CONFIG_KEY =
        new ConfigHandler.ConfigKey<>(ShutdownConfig.class, "shutdown_config");

    public static final ConfigHandler.ConfigKey<StartupConfig> STARTUP_CONFIG_KEY =
        new ConfigHandler.ConfigKey<>(StartupConfig.class, "startup_config");

    public static final ConfigHandler.ConfigKey<PartyConfig> PARTY_CONFIG_KEY =
        new ConfigHandler.ConfigKey<>(PartyConfig.class, "party_config");

    public static final ConfigHandler.ConfigKey<WhisperConfig> WHISPER_CONFIG_KEY =
        new ConfigHandler.ConfigKey<>(WhisperConfig.class, "whisper_config");

    public static final ConfigHandler.ConfigKey<ChatConfig> CHAT_CONFIG_KEY =
        new ConfigHandler.ConfigKey<>(ChatConfig.class, "chat_config");

    public static final ConfigHandler.ConfigKey<JoinReportConfig> JOIN_REPORT_CONFIG_KEY =
        new ConfigHandler.ConfigKey<>(JoinReportConfig.class, "join_report_config");

    public static final ConfigHandler.ConfigKey<ZombiesConfig> ZOMBIES_CONFIG_KEY =
        new ConfigHandler.ConfigKey<>(ZombiesConfig.class, "zombies_config");

    public static final ConfigHandler.ConfigKey<ModifierCommandConfig> MODIFIER_COMMAND_CONFIG_KEY =
        new ConfigHandler.ConfigKey<>(ModifierCommandConfig.class, "modifier_command_config");

    private static ConfigHandler handler;

    private ConfigFeature() {
        throw new UnsupportedOperationException();
    }

    /**
     * Initializes server configuration features. Should only be called once from
     * {@link PhantazmServer#main(String[])}.
     */
    static void initialize(@NotNull MappingProcessorSource mappingProcessorSource) {
        handler = new BasicConfigHandler();

        ConfigCodec tomlCodec = new TomlCodec();
        ConfigCodec yamlCodec = new YamlCodec();
        handler.registerLoader(PLAYER_CONFIG_KEY,
            new SyncFileConfigLoader<>(mappingProcessorSource.processorFor(Token.ofClass(PlayerConfig.class)),
                PlayerConfig.DEFAULT, PLAYER_CONFIG_PATH, tomlCodec));

        handler.registerLoader(SERVER_CONFIG_KEY,
            new SyncFileConfigLoader<>(mappingProcessorSource.processorFor(Token.ofClass(ServerConfig.class)),
                ServerConfig.DEFAULT, SERVER_CONFIG_PATH, tomlCodec));

        handler.registerLoader(LOBBIES_CONFIG_KEY,
            new SyncFileConfigLoader<>(mappingProcessorSource.processorFor(Token.ofClass(LobbiesConfig.class)),
                LobbiesConfig.DEFAULT, LOBBIES_CONFIG_PATH, yamlCodec));

        handler.registerLoader(PATHFINDER_CONFIG_KEY,
            new SyncFileConfigLoader<>(mappingProcessorSource.processorFor(Token.ofClass(PathfinderConfig.class)),
                PathfinderConfig.DEFAULT, PATHFINDER_CONFIG_PATH, tomlCodec));

        handler.registerLoader(SHUTDOWN_CONFIG_KEY,
            new SyncFileConfigLoader<>(mappingProcessorSource.processorFor(Token.ofClass(ShutdownConfig.class)),
                ShutdownConfig.DEFAULT, SHUTDOWN_CONFIG_PATH, tomlCodec));

        handler.registerLoader(STARTUP_CONFIG_KEY,
            new SyncFileConfigLoader<>(mappingProcessorSource.processorFor(Token.ofClass(StartupConfig.class)),
                StartupConfig.DEFAULT, STARTUP_CONFIG_PATH, tomlCodec));

        handler.registerLoader(PARTY_CONFIG_KEY,
            new SyncFileConfigLoader<>(mappingProcessorSource.processorFor(Token.ofClass(PartyConfig.class)),
                PartyConfig.DEFAULT, PARTY_CONFIG_PATH, tomlCodec));

        handler.registerLoader(WHISPER_CONFIG_KEY,
            new SyncFileConfigLoader<>(mappingProcessorSource.processorFor(Token.ofClass(WhisperConfig.class)),
                WhisperConfig.DEFAULT, WHISPER_CONFIG_PATH, tomlCodec));

        handler.registerLoader(CHAT_CONFIG_KEY,
            new SyncFileConfigLoader<>(mappingProcessorSource.processorFor(Token.ofClass(ChatConfig.class)),
                ChatConfig.DEFAULT, CHAT_CONFIG_PATH, yamlCodec));

        handler.registerLoader(JOIN_REPORT_CONFIG_KEY,
            new SyncFileConfigLoader<>(mappingProcessorSource.processorFor(Token.ofClass(JoinReportConfig.class)),
                JoinReportConfig.DEFAULT, JOIN_REPORT_CONFIG_PATH, tomlCodec));

        handler.registerLoader(ZOMBIES_CONFIG_KEY,
            new SyncFileConfigLoader<>(mappingProcessorSource.processorFor(Token.ofClass(ZombiesConfig.class)),
                ZombiesConfig.DEFAULT, ZOMBIES_CONFIG_PATH, tomlCodec));

        handler.registerLoader(MODIFIER_COMMAND_CONFIG_KEY,
            new SyncFileConfigLoader<>(mappingProcessorSource.processorFor(Token.ofClass(ModifierCommandConfig.class)),
                ModifierCommandConfig.DEFAULT, MODIFIER_COMMAND_CONFIG_PATH, tomlCodec));
    }

    /**
     * Returns the {@link ConfigHandler} used by Phantazm.
     *
     * @return the global ConfigHandler
     */
    public static @NotNull ConfigHandler getHandler() {
        return FeatureUtils.check(handler);
    }
}
