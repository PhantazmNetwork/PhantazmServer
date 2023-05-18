package org.phantazm.server.config.loader;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ConfigProcessors;
import org.phantazm.server.config.server.*;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * {@link ConfigProcessor} used for {@link ServerConfig}s.
 */
public class ServerConfigProcessor implements ConfigProcessor<ServerConfig> {

    private static final ConfigProcessor<Component> COMPONENT_PROCESSOR = ConfigProcessors.component();

    private static final ConfigProcessor<TimeUnit> TIME_UNIT_PROCESSOR = ConfigProcessor.enumProcessor(TimeUnit.class);

    @Override
    public @NotNull ServerConfig dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        ConfigNode serverInfo = element.getNodeOrThrow("serverInfo");

        String serverAddress = serverInfo.getStringOrThrow("serverIP");
        int port = serverInfo.getNumberOrThrow("port").intValue();
        if (port < 0 || port > 65535) {
            throw new ConfigProcessException("Invalid port: " + port + ", must be in range [0, 65535]");
        }

        boolean optifineEnabled = serverInfo.getBooleanOrThrow("optifineEnabled");
        AuthType authType = AuthType.getByName(serverInfo.getStringOrThrow("authType").toUpperCase(Locale.ENGLISH))
                .orElseThrow(() -> new ConfigProcessException(
                        "Invalid AuthType, must be one of the following: " + Arrays.toString(AuthType.values())));
        String proxySecret = serverInfo.getStringOrThrow("proxySecret");

        ServerInfoConfig serverInfoConfig =
                new ServerInfoConfig(serverAddress, port, optifineEnabled, authType, proxySecret);

        ConfigNode pingList = element.getNodeOrThrow("pingList");
        Component description = COMPONENT_PROCESSOR.dataFromElement(pingList.getElementOrThrow("description"));
        PingListConfig pingListConfig = new PingListConfig(description);

        ConfigNode pathfinderNode = element.getNodeOrThrow("pathfinder");
        int threads = pathfinderNode.getNumberOrThrow("threads").intValue();
        boolean asyncMode = pathfinderNode.getBooleanOrThrow("asyncMode");
        int corePoolSize = pathfinderNode.getNumberOrThrow("corePoolSize").intValue();
        int maximumPoolSize = pathfinderNode.getNumberOrThrow("maximumPoolSize").intValue();
        int minimumRunnable = pathfinderNode.getNumberOrThrow("minimumRunnable").intValue();
        long keepAliveTime = pathfinderNode.getNumberOrThrow("keepAliveTime").longValue();
        TimeUnit keepAliveTimeUnit =
                TIME_UNIT_PROCESSOR.dataFromElement(pathfinderNode.getElementOrThrow("keepAliveTimeUnit"));

        PathfinderConfig pathfinderConfig =
                new PathfinderConfig(threads, asyncMode, corePoolSize, maximumPoolSize, minimumRunnable, keepAliveTime,
                        keepAliveTimeUnit);

        return new ServerConfig(serverInfoConfig, pingListConfig, pathfinderConfig);
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull ServerConfig serverConfig) throws ConfigProcessException {
        ConfigNode serverInfo = new LinkedConfigNode(5);
        ServerInfoConfig serverInfoConfig = serverConfig.serverInfoConfig();
        serverInfo.putString("serverIP", serverInfoConfig.serverIP());
        serverInfo.putNumber("port", serverInfoConfig.port());
        serverInfo.putBoolean("optifineEnabled", serverInfoConfig.optifineEnabled());
        serverInfo.putString("authType", serverInfoConfig.authType().name());
        serverInfo.putString("proxySecret", serverInfoConfig.proxySecret());

        ConfigNode pingList = new LinkedConfigNode(1);
        PingListConfig pingListConfig = serverConfig.pingListConfig();
        pingList.put("description", COMPONENT_PROCESSOR.elementFromData(pingListConfig.description()));

        ConfigNode pathfinder = new LinkedConfigNode(7);
        PathfinderConfig pathfinderConfig = serverConfig.pathfinderConfig();
        pathfinder.putNumber("threads", pathfinderConfig.threads());
        pathfinder.putBoolean("asyncMode", pathfinderConfig.asyncMode());
        pathfinder.putNumber("corePoolSize", pathfinderConfig.corePoolSize());
        pathfinder.putNumber("maximumPoolSize", pathfinderConfig.maximumPoolSize());
        pathfinder.putNumber("minimumRunnable", pathfinderConfig.minimumRunnable());
        pathfinder.putNumber("keepAliveTime", pathfinderConfig.keepAliveTime());
        pathfinder.put("keepAliveTimeUnit", TIME_UNIT_PROCESSOR.elementFromData(pathfinderConfig.keepAliveTimeUnit()));


        ConfigNode configNode = new LinkedConfigNode(3);
        configNode.put("serverInfo", serverInfo);
        configNode.put("pingList", pingList);
        configNode.put("pathfinder", pathfinder);

        return configNode;
    }

}
