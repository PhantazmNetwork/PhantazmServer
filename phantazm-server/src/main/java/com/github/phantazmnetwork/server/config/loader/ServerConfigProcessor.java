package com.github.phantazmnetwork.server.config.loader;

import com.github.phantazmnetwork.api.config.loader.ConfigProcessor;
import com.github.phantazmnetwork.server.config.server.AuthType;
import com.github.phantazmnetwork.server.config.server.PingListConfig;
import com.github.phantazmnetwork.server.config.server.ServerConfig;
import com.github.phantazmnetwork.server.config.server.ServerInfoConfig;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
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
    public @NotNull ServerConfig createConfigFromElement(@NotNull ConfigElement configElement) {
        ConfigNode serverInfo = configElement.getNodeOrDefault(LinkedConfigNode::new, "serverInfo");
        String serverIP = serverInfo.getStringOrDefault("0.0.0.0", "serverIP");
        int port = serverInfo.getNumberOrDefault(25565, "port").intValue();
        boolean optifineEnabled = serverInfo.getBooleanOrDefault(true, "optifineEnabled");
        AuthType authType = AuthType.getByName(serverInfo.getStringOrDefault(AuthType.MOJANG.name(), "authType")
                .toUpperCase(Locale.ENGLISH)).orElse(AuthType.MOJANG);
        String velocitySecret = serverInfo.getStringOrDefault("", "velocitySecret");
        ServerInfoConfig serverInfoConfig = new ServerInfoConfig(serverIP, port, optifineEnabled, authType,
                velocitySecret);

        ConfigNode pingList = configElement.getNodeOrDefault(LinkedConfigNode::new, "pingList");
        Component description = miniMessage.parse(pingList.getStringOrDefault("", "description"));
        PingListConfig pingListConfig = new PingListConfig(description);

        return new ServerConfig(serverInfoConfig, pingListConfig);
    }

    @Override
    public @NotNull ConfigElement createNodeFromConfig(@NotNull ServerConfig config) {
        ConfigNode serverInfo = new LinkedConfigNode();
        ServerInfoConfig serverInfoConfig = config.serverInfoConfig();
        serverInfo.put("serverIP", new ConfigPrimitive(serverInfoConfig.serverIP()));
        serverInfo.put("port", new ConfigPrimitive(serverInfoConfig.port()));
        serverInfo.put("optifineEnabled", new ConfigPrimitive(serverInfoConfig.optifineEnabled()));
        serverInfo.put("authType", new ConfigPrimitive(serverInfoConfig.authType().name()));
        serverInfo.put("velocitySecret", new ConfigPrimitive(serverInfoConfig.velocitySecret()));

        ConfigNode pingList = new LinkedConfigNode();
        PingListConfig pingListConfig = config.pingListConfig();
        String description = miniMessage.serialize(pingListConfig.description());
        pingList.put("description", new ConfigPrimitive(description));

        ConfigNode configNode = new LinkedConfigNode();
        configNode.put("serverInfo", serverInfo);
        configNode.put("pingList", pingList);

        return configNode;
    }

}
