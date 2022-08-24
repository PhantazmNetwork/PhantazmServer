package com.github.phantazmnetwork.zombies.game;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.core.game.scene.InstanceScene;
import com.github.phantazmnetwork.core.game.scene.RouteResult;
import com.github.phantazmnetwork.core.game.scene.fallback.SceneFallback;
import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.game.player.state.ZombiesPlayerStateKeys;
import com.github.phantazmnetwork.zombies.game.player.state.context.DeadPlayerStateContext;
import com.github.phantazmnetwork.zombies.game.player.state.context.NoContext;
import com.github.phantazmnetwork.zombies.game.stage.Stage;
import com.github.phantazmnetwork.zombies.map.MapSettingsInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class ZombiesScene extends InstanceScene<ZombiesJoinRequest> {

    private final Map<UUID, ZombiesPlayer> zombiesPlayers;

    private final MapSettingsInfo mapSettingsInfo;

    private final StageTransition stageTransition;

    private final Function<? super PlayerView, ? extends ZombiesPlayer> playerCreator;

    private final Random random;

    private boolean joinable;

    public ZombiesScene(@NotNull Map<UUID, ZombiesPlayer> zombiesPlayers, @NotNull Instance instance,
            @NotNull SceneFallback fallback, @NotNull MapSettingsInfo mapSettingsInfo,
            @NotNull StageTransition stageTransition,
            @NotNull Function<? super PlayerView, ? extends ZombiesPlayer> playerCreator, @NotNull Random random) {
        super(instance, fallback);

        this.zombiesPlayers = zombiesPlayers;
        this.mapSettingsInfo = Objects.requireNonNull(mapSettingsInfo, "mapSettingsInfo");
        this.stageTransition = Objects.requireNonNull(stageTransition, "stageTransition");
        this.playerCreator = Objects.requireNonNull(playerCreator, "playerCreator");
        this.random = Objects.requireNonNull(random, "random");
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

        for (ZombiesPlayer zombiesPlayer : oldPlayers) {
            zombiesPlayer.setState(ZombiesPlayerStateKeys.DEAD, DeadPlayerStateContext.rejoin(Collections.emptyList()));
        }

        Vec3I spawn = mapSettingsInfo.origin().add(mapSettingsInfo.spawn());
        Pos pos = new Pos(spawn.getX(), spawn.getY(), spawn.getZ(), mapSettingsInfo.yaw(), mapSettingsInfo.pitch());
        List<Component> messages = mapSettingsInfo.introMessages();
        for (PlayerView view : newPlayers) {
            ZombiesPlayer zombiesPlayer = playerCreator.apply(view);
            zombiesPlayer.start();
            zombiesPlayers.put(view.getUUID(), zombiesPlayer);
            players.put(view.getUUID(), view);

            view.getPlayer().ifPresent(player -> player.setInstance(instance, pos).whenComplete((unused, throwable) -> {
                if (throwable != null) {
                    // todo: error handling
                    return;
                }

                if (!messages.isEmpty()) {
                    player.sendMessage(messages.get(random.nextInt(messages.size())));
                }
            }));
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
            if (stage == null || !stage.hasPermanentPlayers()) {
                zombiesPlayers.remove(leaver);
            }
            else {
                ZombiesPlayer zombiesPlayer = zombiesPlayers.get(leaver);
                if (zombiesPlayer != null) {
                    zombiesPlayer.setState(ZombiesPlayerStateKeys.QUIT, NoContext.INSTANCE);
                }
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
    public void tick(long time) {
        super.tick(time);
        if (!isShutdown() && stageTransition.isComplete()) {
            forceShutdown();
            return;
        }

        for (ZombiesPlayer zombiesPlayer : zombiesPlayers.values()) {
            zombiesPlayer.tick(time);
        }
        stageTransition.tick(time);
    }
}
