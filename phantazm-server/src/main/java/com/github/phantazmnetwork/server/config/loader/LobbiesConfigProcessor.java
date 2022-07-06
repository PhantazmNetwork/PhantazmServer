package com.github.phantazmnetwork.server.config.loader;

import com.github.phantazmnetwork.api.config.InstanceConfig;
import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.server.config.lobby.LobbiesConfig;
import com.github.phantazmnetwork.server.config.lobby.LobbyConfig;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ArrayConfigList;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link ConfigProcessor} used for {@link LobbiesConfig}s.
 */
public class LobbiesConfigProcessor implements ConfigProcessor<LobbiesConfig> {

    private static final ConfigProcessor<Component> COMPONENT_PROCESSOR = AdventureConfigProcessors.component();

    @Override
    public @NotNull LobbiesConfig dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        try {
            Path instancesPath = Path.of(element.getStringOrThrow("instancesPath"));
            Component kickMessage = COMPONENT_PROCESSOR.dataFromElement(element.getElementOrThrow("kickMessage"));
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
        ConfigNode lobbiesNode = new LinkedConfigNode(lobbiesConfig.lobbies().entrySet().size());
        for (Map.Entry<String, LobbyConfig> lobby : lobbiesConfig.lobbies().entrySet()) {
            ConfigNode lobbyNode = new LinkedConfigNode(4);

            ConfigNode spawnPointNode = new LinkedConfigNode(5);
            Pos spawnPoint = lobby.getValue().instanceConfig().spawnPoint();
            spawnPointNode.putNumber("x", spawnPoint.x());
            spawnPointNode.putNumber("y", spawnPoint.y());
            spawnPointNode.putNumber("z", spawnPoint.z());
            spawnPointNode.putNumber("yaw", spawnPoint.yaw());
            spawnPointNode.putNumber("pitch", spawnPoint.pitch());

            ConfigNode instanceConfigNode = new LinkedConfigNode(1);
            instanceConfigNode.put("spawnPoint", spawnPointNode);

            ConfigList lobbyPathsList = new ArrayConfigList(lobby.getValue().lobbyPaths().size());
            for (String subPath : lobby.getValue().lobbyPaths()) {
                lobbyPathsList.addString(subPath);
            }

            lobbyNode.put("instanceConfig", instanceConfigNode);
            lobbyNode.put("lobbyPaths", lobbyPathsList);
            lobbyNode.putNumber("maxPlayers", lobby.getValue().maxPlayers());
            lobbyNode.putNumber("maxLobbies", lobby.getValue().maxLobbies());

            lobbiesNode.put(lobby.getKey(), lobbyNode);
        }

        ConfigNode configNode = new LinkedConfigNode(4);
        configNode.putString("instancesPath", lobbiesConfig.instancesPath().toString());
        configNode.put("kickMessage", AdventureConfigProcessors.component().elementFromData(lobbiesConfig
                .kickMessage()));
        configNode.putString("mainLobbyName", lobbiesConfig.mainLobbyName());
        configNode.put("lobbies", lobbiesNode);

        return configNode;
    }

}
