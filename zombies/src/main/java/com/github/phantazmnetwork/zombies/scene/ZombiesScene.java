package com.github.phantazmnetwork.zombies.scene;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.core.game.scene.InstanceScene;
import com.github.phantazmnetwork.core.game.scene.RouteResult;
import com.github.phantazmnetwork.core.game.scene.fallback.SceneFallback;
import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.zombies.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.player.state.ZombiesPlayerStateKeys;
import com.github.phantazmnetwork.zombies.player.state.context.DeadPlayerStateContext;
import com.github.phantazmnetwork.zombies.player.state.context.NoContext;
import com.github.phantazmnetwork.zombies.stage.Stage;
import com.github.phantazmnetwork.zombies.stage.StageTransition;
import com.github.phantazmnetwork.zombies.map.MapSettingsInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerSocketConnection;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class ZombiesScene extends InstanceScene<ZombiesJoinRequest> {

    private final ZombiesMap map;

    private final Map<UUID, ZombiesPlayer> zombiesPlayers;

    private final MapSettingsInfo mapSettingsInfo;

    private final StageTransition stageTransition;

    private final Function<? super PlayerView, ? extends ZombiesPlayer> playerCreator;

    private boolean joinable;

    public ZombiesScene(@NotNull ZombiesMap map, @NotNull Map<UUID, ZombiesPlayer> zombiesPlayers,
            @NotNull Instance instance, @NotNull SceneFallback fallback, @NotNull MapSettingsInfo mapSettingsInfo,
            @NotNull StageTransition stageTransition,
            @NotNull Function<? super PlayerView, ? extends ZombiesPlayer> playerCreator) {
        super(instance, fallback);

        this.map = Objects.requireNonNull(map, "map");
        this.zombiesPlayers = zombiesPlayers;
        this.mapSettingsInfo = Objects.requireNonNull(mapSettingsInfo, "mapSettingsInfo");
        this.stageTransition = Objects.requireNonNull(stageTransition, "stageTransition");
        this.playerCreator = Objects.requireNonNull(playerCreator, "playerCreator");
    }

    public @NotNull Map<UUID, ZombiesPlayer> getZombiesPlayers() {
        return Map.copyOf(zombiesPlayers);
    }

    public @NotNull MapSettingsInfo getMapSettingsInfo() {
        return mapSettingsInfo;
    }

    public Stage getCurrentStage() {
        return stageTransition.getCurrentStage();
    }

    public boolean isComplete() {
        return stageTransition.isComplete();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull RouteResult join(@NotNull ZombiesJoinRequest joinRequest) {
        Collection<ZombiesPlayer> oldPlayers = new ArrayList<>(joinRequest.getPlayers().size());
        Collection<PlayerView> newPlayers = new ArrayList<>(joinRequest.getPlayers().size());
        for (PlayerView player : joinRequest.getPlayers()) {
            ZombiesPlayer zombiesPlayer = zombiesPlayers.get(player.getUUID());
            if (zombiesPlayer != null) {
                oldPlayers.add(zombiesPlayer);
            }
            else {
                newPlayers.add(player);
            }
        }

        Stage stage = getCurrentStage();
        if (stage != null && stage.hasPermanentPlayers() && !newPlayers.isEmpty()) {
            return new RouteResult(false, Component.text("The game is not accepting new players."));
        }

        if (zombiesPlayers.size() + newPlayers.size() > mapSettingsInfo.maxPlayers()) {
            return new RouteResult(false, Component.text("Too many players!", NamedTextColor.RED));
        }

        for (PlayerView playerView : newPlayers) {
            Optional<Player> player = playerView.getPlayer();
            if (player.isEmpty()) {
                continue;
            }

            boolean hasMinimum = mapSettingsInfo.minimumProtocolVersion() >= 0;
            boolean hasMaximum = mapSettingsInfo.maximumProtocolVersion() >= 0;

            int protocolVersion = MinecraftServer.PROTOCOL_VERSION;
            if (player.get().getPlayerConnection() instanceof PlayerSocketConnection socketConnection) {
                for (GameProfile.Property property : socketConnection.gameProfile().properties()) {
                    if (property.name().equals("protocolVersion")) {
                        try {
                            protocolVersion = Integer.parseInt(property.value());
                        }
                        catch (NumberFormatException ignored) {
                        }
                        break;
                    }
                }
            }

            if (hasMinimum && protocolVersion < mapSettingsInfo.minimumProtocolVersion()) {
                return new RouteResult(false,
                        Component.text("A player's Minecraft version is too old!", NamedTextColor.RED));
            }
            if (hasMaximum && protocolVersion > mapSettingsInfo.maximumProtocolVersion()) {
                return new RouteResult(false,
                        Component.text("A player's Minecraft version is too new!", NamedTextColor.RED));
            }
        }

        for (ZombiesPlayer zombiesPlayer : oldPlayers) {
            zombiesPlayer.setState(ZombiesPlayerStateKeys.DEAD, DeadPlayerStateContext.rejoin());
        }

        Vec3I spawn = mapSettingsInfo.origin().add(mapSettingsInfo.spawn());
        Pos pos = new Pos(spawn.getX(), spawn.getY(), spawn.getZ(), mapSettingsInfo.yaw(), mapSettingsInfo.pitch());
        for (PlayerView view : newPlayers) {
            view.getPlayer().ifPresent(player -> {
                player.setInstance(instance, pos).whenComplete((unused, throwable) -> {
                    if (throwable != null) {
                        // todo: error handling
                        return;
                    }

                    ZombiesPlayer zombiesPlayer = playerCreator.apply(view);
                    zombiesPlayer.start();
                    zombiesPlayer.setState(ZombiesPlayerStateKeys.ALIVE, NoContext.INSTANCE);
                    zombiesPlayers.put(view.getUUID(), zombiesPlayer);
                    players.put(view.getUUID(), view);

                    stage.onJoin(zombiesPlayer);
                });
            });
        }

        return RouteResult.SUCCESSFUL;
    }

    @Override
    public @NotNull RouteResult leave(@NotNull Iterable<UUID> leavers) {
        for (UUID leaver : leavers) {
            if (!players.containsKey(leaver)) {
                return new RouteResult(false,
                        Component.text("Not all players are within the scene.", NamedTextColor.RED));
            }
        }

        for (UUID leaver : leavers) {
            players.remove(leaver);

            Stage stage = getCurrentStage();
            ZombiesPlayer zombiesPlayer;
            if (stage == null || !stage.hasPermanentPlayers()) {
                zombiesPlayer = zombiesPlayers.remove(leaver);
            }
            else {
                zombiesPlayer = zombiesPlayers.get(leaver);
            }

            if (zombiesPlayer != null) {
                zombiesPlayer.setState(ZombiesPlayerStateKeys.QUIT, NoContext.INSTANCE);
            }
        }

        return RouteResult.SUCCESSFUL;
    }

    @Override
    public boolean isJoinable() {
        return joinable;
    }

    @Override
    public void setJoinable(boolean joinable) {
        this.joinable = joinable;
    }

    @Override
    public int getJoinWeight(@NotNull ZombiesJoinRequest request) {
        Stage stage = getCurrentStage();
        if (stage == null || stage.hasPermanentPlayers()) {
            return Integer.MIN_VALUE;
        }

        return 0;
    }

    @Override
    public void forceShutdown() {
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers.values()) {
            zombiesPlayer.end();
        }
        super.forceShutdown();
    }

    @Override
    public void tick(long time) {
        super.tick(time);
        if (!isShutdown() && stageTransition.isComplete()) {
            forceShutdown();
            return;
        }

        map.tick(time);
        stageTransition.tick(time);
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers.values()) {
            zombiesPlayer.tick(time);
        }
    }
}