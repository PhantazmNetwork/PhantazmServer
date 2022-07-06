package com.github.phantazmnetwork.server.config.loader;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.server.config.server.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * {@link ConfigProcessor} used for {@link ServerConfig}s.
 */
public class ServerConfigProcessor implements ConfigProcessor<ServerConfig> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfigProcessor.class);
    private static final String UNSAFE_ARGUMENT = "unsafe";

    private final static ConfigProcessor<Component> COMPONENT_PROCESSOR = AdventureConfigProcessors.component();

    private final String[] args;

    public ServerConfigProcessor(String @NotNull[] args) {
        this.args = Objects.requireNonNull(args, "args");
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
        if(isUnsafe(args)) {
            LOGGER.warn("""
                                                                                                                              
                                          ██░░██                                     
                                        ██░░░░░░██                                  
                                      ██░░░░░░░░░░██                                    
                                      ██░░░░░░░░░░██                                    
                                    ██░░░░░░░░░░░░░░██                                  
                                  ██░░░░░░██████░░░░░░██                                
                                  ██░░░░░░██████░░░░░░██                                
                                ██░░░░░░░░██████░░░░░░░░██                              
                                ██░░░░░░░░██████░░░░░░░░██                              
                              ██░░░░░░░░░░██████░░░░░░░░░░██                            
                            ██░░░░░░░░░░░░██████░░░░░░░░░░░░██                          
                            ██░░░░░░░░░░░░██████░░░░░░░░░░░░██                          
                          ██░░░░░░░░░░░░░░██████░░░░░░░░░░░░░░██                        
                          ██░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░██                        
                        ██░░░░░░░░░░░░░░░░██████░░░░░░░░░░░░░░░░██                      
                        ██░░░░░░░░░░░░░░░░██████░░░░░░░░░░░░░░░░██                      
                      ██░░░░░░░░░░░░░░░░░░██████░░░░░░░░░░░░░░░░░░██                    
                      ██░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░██                    
                        ██████████████████████████████████████████                      
                    """);
            LOGGER.warn("Server starting in unsafe mode! Your proxy secret may be set to the default value (an empty " +
                    "string). Only use this option when running in a development environment.");
        }
        else if(authType == AuthType.VELOCITY && velocitySecret.equals(ServerInfoConfig.DEFAULT_VELOCITY_SECRET)) {
            throw new ConfigProcessException("When using AuthType.VELOCITY, velocitySecret must be set to a value " +
                    "other than the default for security reasons");
        }

        ServerInfoConfig serverInfoConfig = new ServerInfoConfig(serverAddress, port, optifineEnabled, authType,
                velocitySecret);

        ConfigNode pingList = element.getNodeOrThrow("pingList");
        Component description = COMPONENT_PROCESSOR.dataFromElement(pingList.getElementOrThrow("description"));
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
    public @NotNull ConfigElement elementFromData(@NotNull ServerConfig serverConfig) throws ConfigProcessException {
        ConfigNode serverInfo = new LinkedConfigNode(5);
        ServerInfoConfig serverInfoConfig = serverConfig.serverInfoConfig();
        serverInfo.putString("serverIP", serverInfoConfig.serverIP());
        serverInfo.putNumber("port", serverInfoConfig.port());
        serverInfo.putBoolean("optifineEnabled", serverInfoConfig.optifineEnabled());
        serverInfo.putString("authType", serverInfoConfig.authType().name());
        serverInfo.putString("velocitySecret", serverInfoConfig.velocitySecret());

        ConfigNode pingList = new LinkedConfigNode(1);
        PingListConfig pingListConfig = serverConfig.pingListConfig();
        pingList.put("description", COMPONENT_PROCESSOR.elementFromData(pingListConfig.description()));

        ConfigNode configNode = new LinkedConfigNode(2);
        configNode.put("serverInfo", serverInfo);
        configNode.put("pingList", pingList);

        return configNode;
    }

    private static boolean isUnsafe(String[] args) {
        for(String arg : args) {
            if(arg.equals(UNSAFE_ARGUMENT)) {
                return true;
            }
        }

        return false;
    }
}
