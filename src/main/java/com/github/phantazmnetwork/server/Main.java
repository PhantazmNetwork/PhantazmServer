package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.server.config.loader.FileSystemConfigLoader;
import com.github.phantazmnetwork.server.config.server.ServerConfig;
import com.github.phantazmnetwork.server.world.FileSystemWorldLoader;
import com.github.phantazmnetwork.server.world.WorldLoader;
import com.github.phantazmnetwork.server.config.loader.ConfigWriteException;
import com.github.steanky.ethylene.codec.toml.TomlCodec;
import com.github.phantazmnetwork.server.config.world.WorldsConfig;
import com.github.phantazmnetwork.server.config.loader.ConfigLoader;
import com.github.phantazmnetwork.server.config.loader.ConfigProcessor;
import com.github.phantazmnetwork.server.config.loader.ConfigReadException;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.optifine.OptifineSupport;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.AnvilLoader;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Launches the server
 */
public class Main {

    /**
     * Starting point for the server
     * @param args Do you even know java?
     */
    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        Logger logger = LoggerFactory.getLogger(Main.class);

        ServerConfig serverConfig;
        try {
            serverConfig = getServerConfig();
        } catch (ConfigReadException | ConfigWriteException e) {
            logger.error("Failed to read server configuration: ", e);
            return;
        }
        postServerConfigLoad(serverConfig);

        WorldsConfig worldsConfig;
        try {
            worldsConfig = getWorldsConfig();
        }
        catch (ConfigReadException | ConfigWriteException e) {
            logger.error("Failed to read worlds configuration: ", e);
            return;
        }

        Path worldsPath = Paths.get(worldsConfig.worldsPath());
        WorldLoader worldLoader = new FileSystemWorldLoader(worldsPath, AnvilLoader::new);

        Instance lobby = worldLoader.loadWorld(MinecraftServer.getInstanceManager(), worldsConfig.defaultWorldName());

        MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent.class, event -> {
           event.setSpawningInstance(lobby);
           event.getPlayer().setRespawnPoint(worldsConfig.worlds().get(worldsConfig.defaultWorldName()).spawnPoint());
           event.getPlayer().setGameMode(GameMode.ADVENTURE);
        });

        minecraftServer.start(serverConfig.serverInfoConfig().serverIP(), serverConfig.serverInfoConfig().port());
    }

    /**
     * Called after loading the {@link ServerConfig}
     * @param serverConfig The loaded {@link ServerConfig}
     */
    @SuppressWarnings("CodeBlock2Expr")
    private static void postServerConfigLoad(@NotNull ServerConfig serverConfig) {
        if (serverConfig.serverInfoConfig().optifineEnabled()) {
            OptifineSupport.enable();
        }

        switch (serverConfig.serverInfoConfig().authType()) {
            case MOJANG -> MojangAuth.init();
            case BUNGEE -> BungeeCordProxy.enable();
            case VELOCITY ->  VelocityProxy.enable(serverConfig.serverInfoConfig().velocitySecret());
        }

        MinecraftServer.getGlobalEventHandler().addListener(ServerListPingEvent.class, event -> {
            event.getResponseData().setDescription(serverConfig.pingListConfig().description());
        });
    }

    /**
     * Loads server-specific config
     * @return Server-specific config
     * @throws ConfigReadException If reading from config failed
     * @throws ConfigWriteException If writing from config failed
     */
    private static @NotNull ServerConfig getServerConfig() throws ConfigReadException, ConfigWriteException {
        ConfigLoader<ServerConfig> configLoader = new FileSystemConfigLoader<>(Paths.get("./server-config.toml"),
                new TomlCodec(), ConfigProcessor.serverConfigProcessor(MiniMessage.miniMessage()));

        return configLoader.load();
    }

    /**
     * Loads world-specific config
     * @return World-specific config
     * @throws ConfigReadException If reading from config failed
     * @throws ConfigWriteException If writing from config failed
     */
    private static @NotNull WorldsConfig getWorldsConfig() throws ConfigReadException, ConfigWriteException {
        ConfigLoader<WorldsConfig> configLoader = new FileSystemConfigLoader<>(Paths.get("./worlds-config.toml"),
                new TomlCodec(), ConfigProcessor.worldsConfigProcessor());

        return configLoader.load();
    }

}
