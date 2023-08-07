package org.phantazm.zombies.scene;

import com.github.steanky.vector.Vec3I;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.player.PlayerSocketConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.commons.TickTaskScheduler;
import org.phantazm.core.VecUtils;
import org.phantazm.core.game.scene.InstanceScene;
import org.phantazm.core.game.scene.TransferResult;
import org.phantazm.core.game.scene.Utils;
import org.phantazm.core.game.scene.fallback.SceneFallback;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.stats.zombies.ZombiesDatabase;
import org.phantazm.zombies.map.MapSettingsInfo;
import org.phantazm.zombies.map.ZombiesMap;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;
import org.phantazm.zombies.player.state.context.AlivePlayerStateContext;
import org.phantazm.zombies.player.state.context.DeadPlayerStateContext;
import org.phantazm.zombies.stage.Stage;
import org.phantazm.zombies.stage.StageKeys;
import org.phantazm.zombies.stage.StageTransition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ZombiesScene extends InstanceScene<ZombiesJoinRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesScene.class);
    private final ZombiesMap map;
    private final Map<UUID, ZombiesPlayer> zombiesPlayers;
    private final MapSettingsInfo mapSettingsInfo;
    private final StageTransition stageTransition;
    private final LeaveHandler leaveHandler;
    private final Function<? super PlayerView, ? extends ZombiesPlayer> playerCreator;
    private final TickTaskScheduler taskScheduler;
    private final ZombiesDatabase database;
    private final EventNode<Event> sceneNode;
    private final UUID allowedRequestUUID;

    private boolean joinable = true;

    private final Object joinLock = new Object();

    private volatile int pendingPlayers = 0;

    public ZombiesScene(@NotNull UUID uuid, @NotNull ZombiesMap map, @NotNull Map<UUID, ZombiesPlayer> zombiesPlayers,
            @NotNull Instance instance, @NotNull SceneFallback fallback, @NotNull MapSettingsInfo mapSettingsInfo,
            @NotNull StageTransition stageTransition, @NotNull LeaveHandler leaveHandler,
            @NotNull Function<? super PlayerView, ? extends ZombiesPlayer> playerCreator,
            @NotNull TickTaskScheduler taskScheduler, @NotNull ZombiesDatabase database,
            @NotNull EventNode<Event> sceneNode, @Nullable UUID allowedRequestUUID,
            @NotNull PlayerViewProvider playerViewProvider) {
        super(uuid, instance, fallback, VecUtils.toPoint(mapSettingsInfo.spawn()), playerViewProvider);
        this.map = Objects.requireNonNull(map, "map");
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
        this.mapSettingsInfo = Objects.requireNonNull(mapSettingsInfo, "mapSettingsInfo");
        this.stageTransition = Objects.requireNonNull(stageTransition, "stageTransition");
        this.leaveHandler = Objects.requireNonNull(leaveHandler, "leaveHandler");
        this.playerCreator = Objects.requireNonNull(playerCreator, "playerCreator");
        this.taskScheduler = Objects.requireNonNull(taskScheduler, "taskScheduler");
        this.database = Objects.requireNonNull(database, "database");
        this.sceneNode = Objects.requireNonNull(sceneNode, "sceneNode");
        this.allowedRequestUUID = allowedRequestUUID;
    }

    public @NotNull EventNode<Event> getSceneNode() {
        return sceneNode;
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

    public @NotNull ZombiesMap getMap() {
        return map;
    }

    public @NotNull StageTransition getStageTransition() {
        return stageTransition;
    }

    @Override
    public @NotNull TransferResult join(@NotNull ZombiesJoinRequest joinRequest) {
        synchronized (joinLock) {
            if (isShutdown()) {
                return TransferResult.failure(Component.text("Game is shutdown."));
            }
            if (!isJoinable()) {
                return TransferResult.failure(Component.text("Game is not joinable."));
            }
            if (isComplete()) {
                return TransferResult.failure(Component.text("Game is over."));
            }
            if (allowedRequestUUID != null && !joinRequest.getUUID().equals(allowedRequestUUID)) {
                return TransferResult.failure(Component.text("You aren't allowed to join this game."));
            }

            Collection<ZombiesPlayer> oldPlayers = new ArrayList<>(joinRequest.getPlayers().size());
            Collection<PlayerView> newPlayers = new ArrayList<>(joinRequest.getPlayers().size());
            for (PlayerView player : joinRequest.getPlayers()) {
                ZombiesPlayer zombiesPlayer = zombiesPlayers.get(player.getUUID());
                if (zombiesPlayer != null) {
                    if (zombiesPlayer.hasQuit()) {
                        oldPlayers.add(zombiesPlayer);
                    }
                }
                else {
                    newPlayers.add(player);
                }
            }

            Stage stage = getCurrentStage();
            if (stage == null) {
                return TransferResult.failure(Component.text("The game is not currently running.", NamedTextColor.RED));
            }
            if (stage.hasPermanentPlayers() && !newPlayers.isEmpty()) {
                return TransferResult.failure(
                        Component.text("The game is not accepting new players.", NamedTextColor.RED));
            }
            if (!stage.canRejoin() && !oldPlayers.isEmpty()) {
                return TransferResult.failure(
                        Component.text("The game is not accepting rejoining players.", NamedTextColor.RED));
            }

            if (zombiesPlayers.size() + pendingPlayers + newPlayers.size() > mapSettingsInfo.maxPlayers()) {
                return TransferResult.failure(Component.text("Too many players!", NamedTextColor.RED));
            }

            TransferResult protocolResult = checkWithinProtocolVersionBounds(newPlayers);
            if (protocolResult != null) {
                return protocolResult;
            }

            pendingPlayers += newPlayers.size();

            return TransferResult.success(() -> {
                Vec3I spawn = mapSettingsInfo.origin().add(mapSettingsInfo.spawn());
                Pos pos = new Pos(spawn.x(), spawn.y(), spawn.z(), mapSettingsInfo.yaw(), mapSettingsInfo.pitch());
                List<Pair<Player, Instance>> teleportedPlayers = new ArrayList<>(oldPlayers.size() + newPlayers.size());
                List<CompletableFuture<?>> futures = new ArrayList<>(oldPlayers.size() + newPlayers.size());
                List<Runnable> runnables = new ArrayList<>(oldPlayers.size() + newPlayers.size());
                for (ZombiesPlayer zombiesPlayer : oldPlayers) {
                    zombiesPlayer.getPlayer().ifPresent(player -> {
                        teleportOrSetInstance(teleportedPlayers, player, futures, pos);
                        runnables.add(() -> {
                            zombiesPlayer.setState(ZombiesPlayerStateKeys.DEAD, DeadPlayerStateContext.rejoin());
                        });
                    });
                }
                for (PlayerView view : newPlayers) {
                    view.getPlayer().ifPresent(player -> {
                        teleportOrSetInstance(teleportedPlayers, player, futures, pos);
                        runnables.add(() -> {
                            ZombiesPlayer zombiesPlayer = playerCreator.apply(view);
                            zombiesPlayer.start();
                            zombiesPlayer.setState(ZombiesPlayerStateKeys.ALIVE, AlivePlayerStateContext.regular());
                            zombiesPlayers.put(view.getUUID(), zombiesPlayer);
                        });
                    });
                }

                CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).whenComplete((ignored, error) -> {
                    for (int i = 0; i < futures.size(); i++) {
                        Pair<Player, Instance> pair = teleportedPlayers.get(i);
                        Player teleportedPlayer = pair.first();

                        CompletableFuture<?> future = futures.get(i);
                        Runnable runnable = runnables.get(i);

                        if (future.isCompletedExceptionally()) {
                            future.whenComplete((ignored1, throwable) -> {
                                LOGGER.warn("Failed to send {} to an instance", teleportedPlayer.getUuid(), throwable);
                            });
                            continue;
                        }

                        runnable.run();
                        stage.onJoin(zombiesPlayers.get(teleportedPlayer.getUuid()));
                    }
                }).whenComplete((ignored, throwable) -> {
                    if (throwable != null) {
                        LOGGER.warn("Failed to finish player join", throwable);
                    }
                }).join();

                if (allowedRequestUUID != null) {
                    stageTransition.setCurrentStage(StageKeys.IN_GAME);
                }
            }, () -> {
                synchronized (joinLock) {
                    pendingPlayers -= newPlayers.size();
                }
            });
        }
    }

    private void teleportOrSetInstance(List<Pair<Player, Instance>> teleportedPlayers, Player player,
            List<CompletableFuture<?>> futures, Pos pos) {
        teleportedPlayers.add(Pair.of(player, player.getInstance()));
        if (player.getInstance() == instance) {
            futures.add(player.teleport(pos));
            return;
        }

        Instance oldInstance = player.getInstance();
        player.setInstanceAddCallback(() -> Utils.handleInstanceTransfer(oldInstance, instance, player,
                newInstancePlayer -> !super.hasGhost(newInstancePlayer)));
        futures.add(player.setInstance(instance, pos));
    }

    @Override
    public @NotNull TransferResult leave(@NotNull Iterable<UUID> leavers) {
        return leaveHandler.leave(leavers);
    }

    @Override
    public @UnmodifiableView @NotNull Map<UUID, PlayerView> getPlayers() {
        return zombiesPlayers.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().module().getPlayerView()));
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
    public boolean isQuittable() {
        return true;
    }

    private TransferResult checkWithinProtocolVersionBounds(@NotNull Collection<PlayerView> newPlayers) {
        for (PlayerView playerView : newPlayers) {
            Optional<Player> player = playerView.getPlayer();
            if (player.isEmpty()) {
                continue;
            }

            boolean hasMinimum = mapSettingsInfo.minimumProtocolVersion() >= 0;
            boolean hasMaximum = mapSettingsInfo.maximumProtocolVersion() >= 0;

            int protocolVersion = getActualProtocolVersion(player.get().getPlayerConnection());

            if (hasMinimum && protocolVersion < mapSettingsInfo.minimumProtocolVersion()) {
                return TransferResult.failure(
                        Component.text("A player's Minecraft version is too old!", NamedTextColor.RED));
            }
            if (hasMaximum && protocolVersion > mapSettingsInfo.maximumProtocolVersion()) {
                return TransferResult.failure(
                        Component.text("A player's Minecraft version is too new!", NamedTextColor.RED));
            }
        }

        return null;
    }

    @SuppressWarnings("UnstableApiUsage")
    private int getActualProtocolVersion(PlayerConnection playerConnection) {
        int protocolVersion = MinecraftServer.PROTOCOL_VERSION;
        if (!(playerConnection instanceof PlayerSocketConnection socketConnection)) {
            return protocolVersion;
        }

        GameProfile gameProfile = socketConnection.gameProfile();
        if (gameProfile == null) {
            return protocolVersion;
        }

        for (GameProfile.Property property : gameProfile.properties()) {
            if (property.name().equals("protocolVersion")) {
                try {
                    protocolVersion = Integer.parseInt(property.value());
                }
                catch (NumberFormatException ignored) {
                }
                break;
            }
        }

        return protocolVersion;
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
    public void shutdown() {
        if (this.shutdown) {
            return;
        }

        this.shutdown = true;

        List<CompletableFuture<Boolean>> fallbackFutures = new ArrayList<>(zombiesPlayers.size());
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers.values()) {
            database.synchronizeZombiesPlayerMapStats(zombiesPlayer.module().getStats(), zombiesPlayers.size(), null,
                    this.stageTransition.getCurrentStage().key().equals(StageKeys.END) ? map.mapObjects().module()
                            .ticksSinceStart().get() : null);

            if (!zombiesPlayer.hasQuit()) {
                fallbackFutures.add(fallback.fallback(zombiesPlayer.module().getPlayerView())
                        .whenComplete((fallbackResult, throwable) -> {
                            if (throwable != null) {
                                LOGGER.warn("Failed to fallback {}", zombiesPlayer.getUUID(), throwable);
                            }
                        }));
            }

            zombiesPlayer.end();
        }

        stageTransition.end();
        taskScheduler.end();

        zombiesPlayers.clear();
        map.mapObjects().module().powerupHandler().get().end();

        //wait for all players to fallback before we actually shut down the scene
        CompletableFuture.allOf(fallbackFutures.toArray(CompletableFuture[]::new))
                .whenComplete((ignored, error) -> super.shutdown());
    }

    @Override
    public void tick(long time) {
        if (isShutdown()) {
            return;
        }

        if (stageTransition.isComplete()) {
            shutdown();
            return;
        }

        stageTransition.tick(time);
        map.tick(time);
        taskScheduler.tick(time);
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers.values()) {
            if (zombiesPlayer.hasQuit()) {
                continue;
            }

            zombiesPlayer.tick(time);
        }
    }
}
