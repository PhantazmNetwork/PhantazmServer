package com.github.phantazmnetwork.server.config.loader;

import com.github.phantazmnetwork.server.config.server.AuthType;
import com.github.phantazmnetwork.server.config.server.PingListConfig;
import com.github.phantazmnetwork.server.config.server.ServerConfig;
import com.github.phantazmnetwork.server.config.server.ServerInfoConfig;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import com.google.common.net.InetAddresses;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;

/**
 * Config processor used for {@link ServerConfig}s.
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
        ConfigNode serverInfo = element.getNodeOrDefault(LinkedConfigNode::new, "serverInfo");

        String serverAddress = serverInfo.getStringOrDefault(ServerInfoConfig.DEFAULT_SERVER_ADDRESS, "serverIP");

        //noinspection UnstableApiUsage
        if(!InetAddresses.isInetAddress(serverAddress)) {
            throw new ConfigProcessException(serverAddress + " is not a valid InetAddress");
        }

        int port = serverInfo.getNumberOrDefault(ServerInfoConfig.DEFAULT_PORT, "port").intValue();
        boolean optifineEnabled = serverInfo.getBooleanOrDefault(ServerInfoConfig.DEFAULT_OPTIFINE_ENABLED,
                "optifineEnabled");

        AuthType authType = AuthType.getByName(serverInfo.getStringOrDefault(ServerInfoConfig.DEFAULT_AUTH_TYPE.name(),
                "authType").toUpperCase(Locale.ENGLISH)).orElse(ServerInfoConfig.DEFAULT_AUTH_TYPE);
        String velocitySecret = serverInfo.getStringOrDefault(ServerInfoConfig.DEFAULT_VELOCITY_SECRET,
                "velocitySecret");

        ServerInfoConfig serverInfoConfig = new ServerInfoConfig(serverAddress, port, optifineEnabled, authType,
                velocitySecret);

        ConfigNode pingList = element.getNodeOrDefault(LinkedConfigNode::new, "pingList");
        Component description = miniMessage.parse(pingList.getStringOrDefault(PingListConfig.DEFAULT_DESCRIPTION_STRING,
                "description"));
        PingListConfig pingListConfig = new PingListConfig(description);

        return new ServerConfig(serverInfoConfig, pingListConfig);
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