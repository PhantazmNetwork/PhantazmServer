package org.phantazm.zombies.scene2;

import com.github.steanky.vector.Vec3I;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.Instance;
import net.minestom.server.thread.Acquirable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.core.scene2.InstanceScene;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.core.scene2.TablistLocalScene;
import org.phantazm.core.player.PlayerView;
import org.phantazm.stats.zombies.ZombiesDatabase;
import org.phantazm.zombies.Stages;
import org.phantazm.zombies.map.MapSettingsInfo;
import org.phantazm.zombies.map.ZombiesMap;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;
import org.phantazm.zombies.player.state.context.AlivePlayerStateContext;
import org.phantazm.zombies.player.state.context.DeadPlayerStateContext;
import org.phantazm.zombies.player.state.context.QuitPlayerStateContext;
import org.phantazm.zombies.stage.Stage;
import org.phantazm.zombies.stage.StageTransition;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ZombiesScene extends InstanceScene implements TablistLocalScene {
    private final Map<PlayerView, ZombiesPlayer> managedPlayers;
    private final Map<PlayerView, ZombiesPlayer> managedPlayersView;

    private final Map<PlayerView, ZombiesPlayer> activePlayers;
    private final Map<PlayerView, ZombiesPlayer> activePlayersView;

    private final ZombiesMap map;
    private final MapSettingsInfo mapSettingsInfo;
    private final StageTransition stageTransition;
    private final Function<? super PlayerView, ? extends ZombiesPlayer> playerCreator;
    private final ZombiesDatabase database;
    private final EventNode<Event> sceneNode;

    private boolean isLegit;

    public ZombiesScene(@NotNull Instance instance,
        @NotNull ZombiesMap map,
        @NotNull MapSettingsInfo mapSettingsInfo,
        @NotNull Map<PlayerView, ZombiesPlayer> playerMap,
        @NotNull StageTransition stageTransition,
        @NotNull Function<? super PlayerView, ? extends ZombiesPlayer> playerCreator,
        @NotNull ZombiesDatabase database,
        @NotNull EventNode<Event> sceneNode) {
        super(instance, -1);
        this.managedPlayers = Objects.requireNonNull(playerMap);
        this.managedPlayersView = Collections.unmodifiableMap(playerMap);

        this.activePlayers = new HashMap<>();
        this.activePlayersView = Collections.unmodifiableMap(this.activePlayers);

        this.map = Objects.requireNonNull(map);
        this.mapSettingsInfo = Objects.requireNonNull(mapSettingsInfo);
        this.stageTransition = Objects.requireNonNull(stageTransition);
        this.playerCreator = Objects.requireNonNull(playerCreator);
        this.database = Objects.requireNonNull(database);
        this.sceneNode = Objects.requireNonNull(sceneNode);

        this.isLegit = true;
    }

    @Override
    public @NotNull @UnmodifiableView Set<@NotNull PlayerView> playersView() {
        return activePlayersView.keySet();
    }

    @Override
    public boolean preventsServerShutdown() {
        return isLegit;
    }

    void join(@NotNull Set<@NotNull PlayerView> joiningPlayers) {
        CompletableFuture<?>[] futures = new CompletableFuture[joiningPlayers.size()];

        Vec3I spawnBlock = mapSettingsInfo.origin().add(mapSettingsInfo.spawn());
        Pos spawnPos = new Pos(spawnBlock.x() + 0.5, spawnBlock.y(), spawnBlock.z() + 0.5, mapSettingsInfo.yaw(),
            mapSettingsInfo.pitch());

        int i = 0;
        for (PlayerView playerView : joiningPlayers) {
            ZombiesPlayer zombiesPlayer = managedPlayers.get(playerView);
            if (zombiesPlayer == null) {
                futures[i++] = handleNewPlayer(playerView, spawnPos);
                continue;
            }

            if (!zombiesPlayer.hasQuit()) {
                //player exists and hasn't quit, so don't do anything
                futures[i++] = CompletableFuture.completedFuture(null);
                continue;
            }

            //rejoin logic
            futures[i++] = handleRejoiningPlayer(zombiesPlayer, spawnPos);
        }

        CompletableFuture.allOf(futures).join();
    }

    private CompletableFuture<?> handleNewPlayer(PlayerView newPlayer, Pos spawnPos) {
        Optional<Player> playerOptional = newPlayer.getPlayer();
        if (playerOptional.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        Player player = playerOptional.get();
        player.stateHolder().setStage(Stages.ZOMBIES_GAME);

        ZombiesPlayer zombiesPlayer = playerCreator.apply(newPlayer);
        zombiesPlayer.start();
        zombiesPlayer.setState(ZombiesPlayerStateKeys.ALIVE, AlivePlayerStateContext.regular());

        managedPlayers.put(newPlayer, zombiesPlayer);
        activePlayers.put(newPlayer, zombiesPlayer);

        Stage stage = currentStage();
        if (stage != null) {
            stage.onJoin(zombiesPlayer);
        }

        return teleportOrSetInstance(player, spawnPos);
    }

    private CompletableFuture<?> handleRejoiningPlayer(ZombiesPlayer zombiesPlayer, Pos spawnPos) {
        Optional<Player> playerOptional = zombiesPlayer.getPlayer();
        if (playerOptional.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        Player player = playerOptional.get();

        player.stateHolder().setStage(Stages.ZOMBIES_GAME);
        zombiesPlayer.setState(ZombiesPlayerStateKeys.DEAD, DeadPlayerStateContext.rejoin());

        activePlayers.put(zombiesPlayer.module().getPlayerView(), zombiesPlayer);

        Stage stage = currentStage();
        if (stage != null) {
            stage.onJoin(zombiesPlayer);
        }

        return teleportOrSetInstance(player, spawnPos);
    }

    @Override
    public @NotNull Set<@NotNull PlayerView> leave(@NotNull Set<? extends @NotNull PlayerView> players) {
        Stage stage = stageTransition.getCurrentStage();

        Set<PlayerView> leftPlayers = new HashSet<>();
        for (PlayerView player : players) {
            ZombiesPlayer leavingZombiesPlayer = (stage == null || !stage.hasPermanentPlayers()) ?
                managedPlayers.remove(player) : managedPlayers.get(player);
            activePlayers.remove(player);

            if (leavingZombiesPlayer == null) {
                continue;
            }

            leftPlayers.add(player);

            if (stage != null) {
                stage.onLeave(leavingZombiesPlayer);
            }

            leavingZombiesPlayer.setState(ZombiesPlayerStateKeys.QUIT, new QuitPlayerStateContext(true));
        }

        return leftPlayers;
    }

    @Override
    public void tick(long time) {
        super.tick(time);

        if (stageTransition.isComplete()) {
            SceneManager.Global.instance().removeScene(this);
            return;
        }

        stageTransition.tick(time);
        map.tick(time);

        for (ZombiesPlayer zombiesPlayer : activePlayers.values()) {
            zombiesPlayer.tick(time);
        }
    }

    @Override
    public void preShutdown() {
        super.preShutdown();
        if (!isLegit) {
            return;
        }

        for (ZombiesPlayer zombiesPlayer : managedPlayers.values()) {
            database.synchronizeZombiesPlayerMapStats(zombiesPlayer.module().getStats());
        }
    }

    @Override
    public void shutdown() {
        EventNode<? super Event> node = sceneNode.getParent();
        if (node != null) {
            node.removeChild(sceneNode);
        }

        super.shutdown();

        stageTransition.end();
        map.mapObjects().module().powerupHandler().get().end();
    }

    @Override
    protected boolean canTimeout() {
        //timeout is handled by StageTransition
        return false;
    }

    public @NotNull @UnmodifiableView Map<PlayerView, ZombiesPlayer> managedPlayers() {
        return managedPlayersView;
    }

    public @NotNull ZombiesMap map() {
        return map;
    }

    public @NotNull MapSettingsInfo mapSettingsInfo() {
        return mapSettingsInfo;
    }

    public @NotNull StageTransition stageTransition() {
        return stageTransition;
    }

    public boolean isLegit() {
        return isLegit;
    }

    public void setLegit(boolean legit) {
        this.isLegit = legit;
    }

    public @NotNull EventNode<Event> sceneNode() {
        return sceneNode;
    }

    public @Nullable Stage currentStage() {
        return stageTransition.getCurrentStage();
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull Acquirable<? extends ZombiesScene> getAcquirable() {
        return (Acquirable<? extends ZombiesScene>) super.getAcquirable();
    }
}