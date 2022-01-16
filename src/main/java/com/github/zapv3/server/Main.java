package com.github.zapv3.server;

import com.github.steanky.ethylene.codec.toml.TomlCodec;
import com.github.zapv3.server.config.PingListConfig;
import com.github.zapv3.server.config.ServerConfig;
import com.github.zapv3.server.config.loader.ServerConfigLoader;
import com.github.zapv3.server.config.loader.ServerConfigProcessor;
import com.github.zapv3.server.config.loader.ServerConfigReadException;
import com.github.zapv3.server.config.loader.ServerConfigWriteException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extras.optifine.OptifineSupport;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.function.Supplier;

public class Main {

    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        ServerConfig config = getServerConfig();
        if (config.optifineEnabled()) {
            OptifineSupport.enable();
        }

        MinecraftServer.getGlobalEventHandler().addListener(ServerListPingEvent.class, event -> {
            event.getResponseData().setDescription(config.pingListConfig().description());
        });
        minecraftServer.start(config.serverIP(), config.port());
    }

    private static @NotNull ServerConfig getServerConfig() {
        Supplier<ServerConfig> defaultConfig = () -> new ServerConfig(new PingListConfig(Component.empty()),
                "0.0.0.0", 25565, true);
        ServerConfigLoader serverConfigLoader = ServerConfigLoader.defaultLoader(Paths.get("./server-config.toml"),
                ServerConfigProcessor.defaultReader(MiniMessage.get(), new TomlCodec()), defaultConfig);

        try {
            return serverConfigLoader.load();
        } catch (ServerConfigReadException | ServerConfigWriteException e) {
            LoggerFactory.getLogger(Main.class).warn("Failed to load config from server-config.toml, using default " +
                    "config: ", e);
            return defaultConfig.get();
        }
    }

}
