package com.github.phantazmnetwork.server.config.loader;

import com.github.phantazmnetwork.server.config.server.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * {@link ConfigProcessor} used for {@link ServerConfig}s.
 */
@SuppressWarnings("ClassCanBeRecord")
public class ServerConfigProcessor implements ConfigProcessor<ServerConfig> {

    private final MiniMessage miniMessage;

    /**
     * Creates a processor for {@link ServerConfig}.
     * @param miniMessage A {@link MiniMessage} instance used to parse {@link Component}s
     */
    public ServerConfigProcessor(@NotNull MiniMessage miniMessage) {
        this.miniMessage = Objects.requireNonNull(miniMessage, "miniMessage");
    }

    @Override
    public @NotNull ServerConfig dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        ConfigNode serverInfo = element.getNodeOrThrow("serverInfo");

        String serverAddress = serverInfo.getStringOrThrow("serverIP");
        int port = serverInfo.getNumberOrThrow("port").intValue();
        if (port < 0  || port > 65535) {
            throw new ConfigProcessException("Invalid port: " + port + ", must be in range [0, 65535]");
        }

        boolean optifineEnabled = serverInfo.getBooleanOrThrow("optifineEnabled");
        AuthType authType = AuthType.getByName(serverInfo.getStringOrThrow("authType")
                .toUpperCase(Locale.ENGLISH)).orElseThrow(() -> new ConfigProcessException("Invalid AuthType, must " +
                "be one of the following: " + Arrays.toString(AuthType.values())));
        String velocitySecret = serverInfo.getStringOrThrow("velocitySecret");
        if(authType == AuthType.VELOCITY && velocitySecret.equals(ServerInfoConfig.DEFAULT_VELOCITY_SECRET)) {
            throw new ConfigProcessException("When using AuthType.VELOCITY, velocitySecret must be set to a value " +
                    "other than the default for security reasons");
        }

        ServerInfoConfig serverInfoConfig = new ServerInfoConfig(serverAddress, port, optifineEnabled, authType,
                velocitySecret);

        ConfigNode pingList = element.getNodeOrThrow("pingList");
        Component description = miniMessage.deserialize(pingList.getStringOrThrow("description"));
        PingListConfig pingListConfig = new PingListConfig(description);

        ConfigNode pathfinderNode = element.getNodeOrThrow("pathfinder");
        int threads = pathfinderNode.getNumberOrThrow("threads").intValue();
        int cacheSize = pathfinderNode.getNumberOrThrow("cacheSize").intValue();
        int updateQueueCapacity = pathfinderNode.getNumberOrThrow("updateQueueCapacity").intValue();
        if(threads < 1) {
            throw new ConfigProcessException("Invalid number of pathfinder threads, must be >= 1");
        }

        if(cacheSize < 0) {
            throw new ConfigProcessException("Pathfinder cache size must be >= 0");
        }

        if(updateQueueCapacity < 0) {
            throw new ConfigProcessException("Update queue capacity must be > 0");
        }

        PathfinderConfig pathfinderConfig = new PathfinderConfig(threads, cacheSize, updateQueueCapacity);
        return new ServerConfig(serverInfoConfig, pingListConfig, pathfinderConfig);
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull ServerConfig serverConfig) {
        ConfigNode serverInfo = new LinkedConfigNode();
        ServerInfoConfig serverInfoConfig = serverConfig.serverInfoConfig();
        serverInfo.put("serverIP", new ConfigPrimitive(serverInfoConfig.serverIP()));
        serverInfo.put("port", new ConfigPrimitive(serverInfoConfig.port()));
        serverInfo.put("optifineEnabled", new ConfigPrimitive(serverInfoConfig.optifineEnabled()));
        serverInfo.put("authType", new ConfigPrimitive(serverInfoConfig.authType().name()));
        serverInfo.put("velocitySecret", new ConfigPrimitive(serverInfoConfig.velocitySecret()));

        ConfigNode pingList = new LinkedConfigNode();
        PingListConfig pingListConfig = serverConfig.pingListConfig();
        String description = miniMessage.serialize(pingListConfig.description());
        pingList.put("description", new ConfigPrimitive(description));

        ConfigNode configNode = new LinkedConfigNode();
        configNode.put("serverInfo", serverInfo);
        configNode.put("pingList", pingList);

        return configNode;
    }
}
