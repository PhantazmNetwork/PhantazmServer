package com.github.zapv3.server.config.loader;

import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.bridge.ConfigBridges;
import com.github.steanky.ethylene.core.codec.ConfigCodec;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.zapv3.server.config.PingListConfig;
import com.github.zapv3.server.config.ServerConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public interface ServerConfigProcessor {

    static @NotNull ServerConfigProcessor defaultReader(@NotNull MiniMessage miniMessage, @NotNull ConfigCodec codec) {
        return new ServerConfigProcessor() {
            @Override
            public @NotNull ServerConfig readConfig(@NotNull Path path) throws ServerConfigReadException {
                try {
                    ConfigNode configNode = ConfigBridges.read(Files.newInputStream(path), codec).asNode();

                    ConfigNode serverInfo = configNode.get("serverInfo").asNode();
                    String serverIP = serverInfo.get("serverIP").asString();
                    int port = serverInfo.get("port").asNumber().intValue();
                    boolean optifineEnabled = serverInfo.get("optifineEnabled").asBoolean();

                    ConfigNode pingList = configNode.get("pingList").asNode();
                    Component description = miniMessage.parse(pingList.get("description").asString());

                    return new ServerConfig(new PingListConfig(description), serverIP, port, optifineEnabled);
                }
                catch (IllegalStateException | IOException e) {
                    throw new ServerConfigReadException(e);
                }
            }

            @Override
            public void writeConfig(@NotNull Path path, @NotNull ServerConfig config)
                    throws ServerConfigWriteException {
                try {
                    ConfigNode serverInfo = new LinkedConfigNode();
                    serverInfo.put("serverIP", new ConfigPrimitive(config.serverIP()));
                    serverInfo.put("port", new ConfigPrimitive(config.port()));
                    serverInfo.put("optifineEnabled", new ConfigPrimitive(config.optifineEnabled()));

                    ConfigNode pingList = new LinkedConfigNode();
                    String description = miniMessage.serialize(config.pingListConfig().description());
                    pingList.put("description", new ConfigPrimitive(description));

                    ConfigNode configNode = new LinkedConfigNode();
                    configNode.put("serverInfo", serverInfo);
                    configNode.put("pingList", pingList);

                    ConfigBridges.write(Files.newOutputStream(path), codec, configNode);
                }
                catch (IOException e) {
                    throw new ServerConfigWriteException(e);
                }
            }
        };
    }

    @NotNull ServerConfig readConfig(@NotNull Path path) throws ServerConfigReadException;

    void writeConfig(@NotNull Path path, @NotNull ServerConfig config) throws ServerConfigWriteException;

}
