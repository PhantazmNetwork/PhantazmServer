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
import org.phantazm.commons.FutureUtils;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.scene2.InstanceScene;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.tick.TickTaskScheduler;
import org.phantazm.stats.zombies.ZombiesDatabase;
import org.phantazm.zombies.Stages;
import org.phantazm.zombies.map.MapSettingsInfo;
import org.phantazm.zombies.map.ZombiesMap;
import org.phantazm.zombies.modifier.Modifier;
import org.phantazm.zombies.modifier.ModifierComponent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;
import org.phantazm.zombies.player.state.context.AlivePlayerStateContext;
import org.phantazm.zombies.player.state.context.DeadPlayerStateContext;
import org.phantazm.zombies.player.state.context.QuitPlayerStateContext;
import org.phantazm.zombies.stage.Stage;
import org.phantazm.zombies.stage.StageKeys;
import org.phantazm.zombies.stage.StageTransition;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class ZombiesScene extends InstanceScene {
    private final Map<PlayerView, ZombiesPlayer> managedPlayers;
    private final Map<PlayerView, ZombiesPlayer> managedPlayersView;

    private final ZombiesMap map;
    private final MapSettingsInfo mapSettingsInfo;
    private final StageTransition stageTransition;
    private final Function<? super PlayerView, ? extends ZombiesPlayer> playerCreator;
    private final ZombiesDatabase database;
    private final EventNode<Event> sceneNode;
    private final TickTaskScheduler tickTaskScheduler;

    private final Pos spawnPos;

    private boolean legit;
    private boolean sandbox;
    private boolean restricted;

    private final Set<ModifierComponent> activeModifiers;
    private final Set<ModifierComponent> activeModifiersView;

    private final List<Modifier> tickingModifiers;

    public ZombiesScene(@NotNull Instance instance,
        @NotNull ZombiesMap map,
        @NotNull MapSettingsInfo mapSettingsInfo,
        @NotNull Map<PlayerView, ZombiesPlayer> playerMap,
        @NotNull StageTransition stageTransition,
        @NotNull Function<? super PlayerView, ? extends ZombiesPlayer> playerCreator,
        @NotNull ZombiesDatabase database,
        @NotNull EventNode<Event> sceneNode,
        @NotNull TickTaskScheduler tickTaskScheduler) {
        super(instance, -1);
        this.managedPlayers = Objects.requireNonNull(playerMap);
        this.managedPlayersView = Collections.unmodifiableMap(playerMap);

        this.map = Objects.requireNonNull(map);
        this.mapSettingsInfo = Objects.requireNonNull(mapSettingsInfo);
        this.stageTransition = Objects.requireNonNull(stageTransition);
        this.playerCreator = Objects.requireNonNull(playerCreator);
        this.database = Objects.requireNonNull(database);
        this.sceneNode = Objects.requireNonNull(sceneNode);
        this.tickTaskScheduler = Objects.requireNonNull(tickTaskScheduler);

        Vec3I spawnBlock = mapSettingsInfo.origin().add(mapSettingsInfo.spawn());
        this.spawnPos = new Pos(spawnBlock.x() + 0.5, spawnBlock.y(), spawnBlock.z() + 0.5, mapSettingsInfo.yaw(),
            mapSettingsInfo.pitch());

        this.legit = true;

        this.activeModifiers = new HashSet<>();
        this.activeModifiersView = Collections.unmodifiableSet(this.activeModifiers);

        this.tickingModifiers = new ArrayList<>();
    }

    @Override
    public boolean preventsServerShutdown() {
        if (!legit) {
            return false;
        }

        if (playerCount() == 0) {
            return false;
        }

        Stage stage = stageTransition.getCurrentStage();
        if (stage == null) {
            return false;
        }

        return stage.preventsShutdown();
    }

    void join(@NotNull Set<@NotNull PlayerView> joiningPlayers) {
        super.scenePlayers.addAll(joiningPlayers);

        CompletableFuture<?>[] futures = new CompletableFuture[joiningPlayers.size()];

        int i = 0;
        for (PlayerView playerView : joiningPlayers) {
            ZombiesPlayer zombiesPlayer = managedPlayers.get(playerView);
            if (zombiesPlayer == null) {
                futures[i++] = handleNewPlayer(playerView, spawnPos);
                continue;
            }

            if (!zombiesPlayer.hasQuit()) {
                //player exists and hasn't quit, so don't do anything
                futures[i++] = FutureUtils.nullCompletedFuture();
                continue;
            }

            //rejoin logic
            futures[i++] = handleRejoiningPlayer(zombiesPlayer, spawnPos);
        }

        CompletableFuture.allOf(futures).join();

        Stage current = stageTransition.getCurrentStage();
        if (restricted && (current == null || current.key().equals(StageKeys.IDLE_STAGE) ||
            current.key().equals(StageKeys.COUNTDOWN))) {
            stageTransition.setCurrentStage(StageKeys.IN_GAME);
        }
    }

    @Override
    protected @NotNull CompletableFuture<?> joinSpectator(@NotNull Player spectator, boolean ghost) {
        return teleportOrSetInstance(spectator, spawnPos);
    }

    private CompletableFuture<?> handleNewPlayer(PlayerView newPlayer, Pos spawnPos) {
        Optional<Player> playerOptional = newPlayer.getPlayer();
        if (playerOptional.isEmpty()) {
            return FutureUtils.nullCompletedFuture();
        }

        Player player = playerOptional.get();

        player.heal();
        player.stateHolder().setStage(Stages.ZOMBIES_GAME);

        ZombiesPlayer zombiesPlayer = playerCreator.apply(newPlayer);
        zombiesPlayer.start();
        zombiesPlayer.setState(ZombiesPlayerStateKeys.ALIVE, AlivePlayerStateContext.regular());

        managedPlayers.put(newPlayer, zombiesPlayer);

        Stage stage = currentStage();
        if (stage != null) {
            stage.onJoin(zombiesPlayer);
        }

        return teleportOrSetInstance(player, spawnPos);
    }

    private CompletableFuture<?> handleRejoiningPlayer(ZombiesPlayer zombiesPlayer, Pos spawnPos) {
        Optional<Player> playerOptional = zombiesPlayer.getPlayer();
        if (playerOptional.isEmpty()) {
            return FutureUtils.nullCompletedFuture();
        }

        Player player = playerOptional.get();

        player.stateHolder().setStage(Stages.ZOMBIES_GAME);
        zombiesPlayer.setState(ZombiesPlayerStateKeys.DEAD, DeadPlayerStateContext.rejoin());

        Stage stage = currentStage();
        if (stage != null) {
            stage.onJoin(zombiesPlayer);
        }

        return teleportOrSetInstance(player, spawnPos);
    }

    @Override
    public @NotNull Set<@NotNull PlayerView> leave(@NotNull Set<? extends @NotNull PlayerView> players) {
        Set<PlayerView> leftPlayers = super.leave(players);

        Stage stage = stageTransition.getCurrentStage();
        for (PlayerView player : players) {
            ZombiesPlayer leavingZombiesPlayer = (stage == null || !stage.hasPermanentPlayers()) ?
                managedPlayers.remove(player) : managedPlayers.get(player);

            if (leavingZombiesPlayer == null) {
                continue;
            }

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
        tickTaskScheduler.tick(time);

        for (ZombiesPlayer zombiesPlayer : managedPlayers.values()) {
            if (zombiesPlayer.hasQuit()) {
                continue;
            }

            zombiesPlayer.tick(time);
        }

        for (Modifier modifier : tickingModifiers) {
            modifier.tick(time);
        }
    }

    @Override
    public void preShutdown() {
        super.preShutdown();

        stageTransition.end();
        tickTaskScheduler.end();
        map.mapObjects().module().powerupHandler().get().end();

        if (!legit || !mapSettingsInfo.trackStats()) {
            return;
        }

        for (ZombiesPlayer zombiesPlayer : managedPlayers.values()) {
            database.synchronizeZombiesPlayerMapStats(zombiesPlayer.module().getStats());
        }
    }

    @Override
    public void shutdown() {
        EventNode<? super Event> parent = sceneNode.getParent();
        if (parent != null) {
            parent.removeChild(sceneNode);
        }

        super.shutdown();
    }

    @Override
    protected boolean canTimeout() {
        //timeout is handled by StageTransition
        return false;
    }

    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }

    public boolean isRestricted() {
        return this.restricted;
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
        return legit;
    }

    public void setLegit(boolean legit) {
        this.legit = legit;
    }

    public boolean isSandbox() {
        return sandbox;
    }

    public void setSandbox(boolean sandbox) {
        this.sandbox = sandbox;

        if (sandbox) {
            this.legit = false;
        }
    }

    public @NotNull EventNode<Event> sceneNode() {
        return sceneNode;
    }

    public void addModifier(@NotNull ModifierComponent modifier, @NotNull InjectionStore injectionStore) {
        Objects.requireNonNull(modifier);
        Objects.requireNonNull(injectionStore);

        this.activeModifiers.add(modifier);

        Modifier actualModifier = modifier.apply(injectionStore, this);
        if (actualModifier.needsTicking()) {
            this.tickingModifiers.add(actualModifier);
        }
    }

    public @NotNull @UnmodifiableView Set<ModifierComponent> activeModifiers() {
        return this.activeModifiersView;
    }

    public void broadcastEvent(@NotNull Event event) {
        sceneNode.call(event);
    }

    public <E extends Event> void addListener(@NotNull Class<E> eventClass, @NotNull Consumer<E> listener) {
        sceneNode.addListener(eventClass, listener);
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