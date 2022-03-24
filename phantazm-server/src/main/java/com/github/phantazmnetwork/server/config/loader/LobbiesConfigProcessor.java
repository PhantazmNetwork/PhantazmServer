package com.github.phantazmnetwork.server.config.loader;

import com.github.phantazmnetwork.api.config.InstanceConfig;
import com.github.phantazmnetwork.server.config.lobby.LobbiesConfig;
import com.github.phantazmnetwork.server.config.lobby.LobbyConfig;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ArrayConfigList;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.*;

/**
 * {@link ConfigProcessor} used for {@link LobbiesConfig}s.
 */
@SuppressWarnings("ClassCanBeRecord")
public class LobbiesConfigProcessor implements ConfigProcessor<LobbiesConfig> {

    private final MiniMessage miniMessage;

    /**
     * Creates a processor for {@link LobbiesConfig}s.
     * @param miniMessage A {@link MiniMessage} instance used to parse {@link Component}s
     */
    public LobbiesConfigProcessor(@NotNull MiniMessage miniMessage) {
        this.miniMessage = Objects.requireNonNull(miniMessage, "miniMessage");
    }

    @Override
    public @NotNull LobbiesConfig dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        try {
            Path instancesPath = Path.of(element.getStringOrThrow("instancesPath"));
            Component kickMessage = miniMessage.parse(element.getStringOrThrow("kickMessage"));
            String mainLobbyName = element.getStringOrThrow("mainLobbyName");

            ConfigNode lobbiesNode = element.getNodeOrThrow("lobbies");
            Map<String, LobbyConfig> lobbies = new HashMap<>(lobbiesNode.size());
            for (Map.Entry<String, ConfigElement> lobby : lobbiesNode.entrySet()) {
                ConfigNode instanceConfigNode = lobby.getValue().getNodeOrThrow("instanceConfig");

                ConfigNode spawnPoint = instanceConfigNode.getNodeOrThrow("spawnPoint");
                double x = spawnPoint.getNumberOrThrow("x").doubleValue();
                double y = spawnPoint.getNumberOrThrow("y").doubleValue();
                double z = spawnPoint.getNumberOrThrow("z").doubleValue();
                float yaw = spawnPoint.getNumberOrThrow("yaw").floatValue();
                float pitch = spawnPoint.getNumberOrThrow("pitch").floatValue();
                InstanceConfig instanceConfig = new InstanceConfig(new Pos(x, y, z, yaw, pitch));

                ConfigList lobbyPathsList = lobby.getValue().getListOrThrow("lobbyPaths");
                List<String> lobbyPaths = new ArrayList<>(lobbyPathsList.size());
                for (int i = 0; i < lobbyPathsList.size(); i++) {
                    lobbyPaths.add(lobbyPathsList.getStringOrThrow(i));
                }

                int maxPlayers = lobby.getValue().getNumberOrThrow("maxPlayers").intValue();
                int maxLobbies = lobby.getValue().getNumberOrThrow("maxLobbies").intValue();

                lobbies.put(lobby.getKey(), new LobbyConfig(instanceConfig, lobbyPaths, maxPlayers, maxLobbies));
            }

            return new LobbiesConfig(instancesPath, kickMessage, mainLobbyName, lobbies);
        }
        catch (InvalidPathException e) {
            throw new ConfigProcessException(e);
        }
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull LobbiesConfig lobbiesConfig) throws ConfigProcessException {
        ConfigNode lobbiesNode = new LinkedConfigNode();
        for (Map.Entry<String, LobbyConfig> lobby : lobbiesConfig.lobbies().entrySet()) {
            ConfigNode lobbyNode = new LinkedConfigNode();

            ConfigNode spawnPointNode = new LinkedConfigNode();
            Pos spawnPoint = lobby.getValue().instanceConfig().spawnPoint();
            spawnPointNode.put("x", new ConfigPrimitive(spawnPoint.x()));
            spawnPointNode.put("y", new ConfigPrimitive(spawnPoint.y()));
            spawnPointNode.put("z", new ConfigPrimitive(spawnPoint.z()));
            spawnPointNode.put("yaw", new ConfigPrimitive(spawnPoint.yaw()));
            spawnPointNode.put("pitch", new ConfigPrimitive(spawnPoint.pitch()));

            ConfigNode instanceConfigNode = new LinkedConfigNode();
            instanceConfigNode.put("spawnPoint", spawnPointNode);

            Collection<ConfigElement> lobbyPathsList = new ArrayList<>(lobby.getValue().lobbyPaths().size());
            for (String subPath : lobby.getValue().lobbyPaths()) {
                lobbyPathsList.add(new ConfigPrimitive(subPath));
            }

            lobbyNode.put("instanceConfig", instanceConfigNode);
            lobbyNode.put("lobbyPaths", new ArrayConfigList(lobbyPathsList));
            lobbyNode.put("maxPlayers", new ConfigPrimitive(lobby.getValue().maxPlayers()));
            lobbyNode.put("maxLobbies", new ConfigPrimitive(lobby.getValue().maxLobbies()));

            lobbiesNode.put(lobby.getKey(), lobbyNode);
        }

        ConfigNode configNode = new LinkedConfigNode();
        configNode.put("instancesPath", new ConfigPrimitive(lobbiesConfig.instancesPath().toString()));
        configNode.put("kickMessage", new ConfigPrimitive(miniMessage.serialize(lobbiesConfig.kickMessage())));
        configNode.put("mainLobbyName", new ConfigPrimitive(lobbiesConfig.mainLobbyName()));
        configNode.put("lobbies", lobbiesNode);

        return configNode;
    }

}
