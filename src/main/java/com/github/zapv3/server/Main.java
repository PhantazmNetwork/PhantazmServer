package com.github.zapv3.server;

import com.github.steanky.ethylene.codec.toml.TomlCodec;
import com.github.zapv3.server.config.server.PingListConfig;
import com.github.zapv3.server.config.server.ServerConfig;
import com.github.zapv3.server.config.server.ServerInfoConfig;
import com.github.zapv3.server.config.world.WorldsConfig;
import com.github.zapv3.server.config.loader.ConfigLoader;
import com.github.zapv3.server.config.loader.ConfigProcessor;
import com.github.zapv3.server.config.loader.ConfigReadException;
import com.github.zapv3.server.config.loader.ConfigWriteException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extras.optifine.OptifineSupport;
import net.minestom.server.instance.AnvilLoader;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.function.Supplier;

public class Main {

    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        ServerConfig serverConfig = getServerConfig();
        if (serverConfig.serverInfoConfig().optifineEnabled()) {
            OptifineSupport.enable();
        }

        MinecraftServer.getGlobalEventHandler().addListener(ServerListPingEvent.class, event -> {
            event.getResponseData().setDescription(serverConfig.pingListConfig().description());
        });

        WorldsConfig worldsConfig = getWorldsConfig();
        Path path = Paths.get(worldsConfig.worldsPath(), worldsConfig.defaultWorldName());
        IChunkLoader chunkLoader = new AnvilLoader(path);

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer lobby = instanceManager.createInstanceContainer(chunkLoader);

        MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent.class, event -> {
           event.setSpawningInstance(lobby);
           event.getPlayer().setRespawnPoint(worldsConfig.worlds().get(worldsConfig.defaultWorldName()).spawnPoint());
        });

        minecraftServer.start(serverConfig.serverInfoConfig().serverIP(), serverConfig.serverInfoConfig().port());
    }

    private static @NotNull ServerConfig getServerConfig() {
        Supplier<ServerConfig> defaultConfig = () -> {
            return new ServerConfig(
                    new ServerInfoConfig("0.0.0.0", 25565, true),
                    new PingListConfig(Component.empty())
            );
        };
        ConfigLoader<ServerConfig> configLoader = ConfigLoader.defaultLoader(
                Paths.get("./server-config.toml"),
                ConfigProcessor.serverConfigProcessor(MiniMessage.get(), new TomlCodec()),
                defaultConfig
        );

        try {
            return configLoader.load();
        }
        catch (ConfigReadException | ConfigWriteException e) {
            LoggerFactory.getLogger(Main.class).warn("Failed to load config from server-config.toml, using default " +
                    "config: ", e);
            return defaultConfig.get();
        }
    }

    private static @NotNull WorldsConfig getWorldsConfig() {
        Supplier<WorldsConfig> defaultConfig = () -> {
            return new WorldsConfig("lobby", "./worlds/", "./maps/",
                    Collections.emptyMap());
        };
        ConfigLoader<WorldsConfig> configLoader = ConfigLoader.defaultLoader(
                Paths.get("./worlds-config.toml"),
                ConfigProcessor.worldsConfigProcessor(new TomlCodec()),
                defaultConfig
        );

        try {
            return configLoader.load();
        }
        catch (ConfigReadException | ConfigWriteException e) {
            LoggerFactory.getLogger(Main.class).warn("Failed to load config from worlds-config.toml, using default " +
                    "config: ", e);
            return defaultConfig.get();
        }
    }

}
