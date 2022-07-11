package com.github.phantazmnetwork.zombies.game;

import com.github.phantazmnetwork.api.game.scene.InstanceScene;
import com.github.phantazmnetwork.api.game.scene.RouteResult;
import com.github.phantazmnetwork.api.game.scene.fallback.SceneFallback;
import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
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

    private final ZombiesMap map;

    private final StageTransition stageTransition;

    private final Function<PlayerView, ZombiesPlayer> playerCreator;

    private final Random random;

    private boolean joinable;

    public ZombiesScene(@NotNull Instance instance, @NotNull SceneFallback fallback, @NotNull ZombiesMap map,
                        @NotNull StageTransition stageTransition,
                        @NotNull Function<PlayerView, ZombiesPlayer> playerCreator,
                        @NotNull Random random) {
        super(instance, fallback);

        this.zombiesPlayers = new HashMap<>(map.getData().info().maxPlayers());
        this.map = Objects.requireNonNull(map, "map");
        this.stageTransition = Objects.requireNonNull(stageTransition, "stageTransition");
        this.playerCreator = Objects.requireNonNull(playerCreator, "playerCreator");
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public @NotNull RouteResult join(@NotNull ZombiesJoinRequest joinRequest) {
        Collection<PlayerView> newPlayers = new ArrayList<>(joinRequest.getPlayers().size());
        for (PlayerView player : joinRequest.getPlayers()) {
            if (zombiesPlayers.containsKey(player.getUUID())) {
                continue;
            }

            newPlayers.add(player);
        }

        MapSettingsInfo mapSettingsInfo = map.getData().info();
        if (zombiesPlayers.size() + newPlayers.size() > mapSettingsInfo.maxPlayers()) {
            return new RouteResult(false, Optional.of(Component.text("Too many players!", NamedTextColor.RED)));
        }

        Vec3I spawn = mapSettingsInfo.origin().add(mapSettingsInfo.spawn());
        Pos pos = new Pos(spawn.getX(), spawn.getY(), spawn.getZ(), mapSettingsInfo.yaw(), mapSettingsInfo.pitch());
        List<Component> messages = mapSettingsInfo.introMessages();
        for (PlayerView view : newPlayers) {
            ZombiesPlayer zombiesPlayer = playerCreator.apply(view);
            zombiesPlayers.put(view.getUUID(), zombiesPlayer);
            players.put(view.getUUID(), view);

            view.getPlayer().ifPresent(player -> {
                player.setInstance(instance, pos).whenComplete((unused, throwable) -> {
                    if (throwable != null) {
                        // todo: error handling
                        return;
                    }

                    player.sendMessage(messages.get(random.nextInt(messages.size())));
                });
            });
        }

        return new RouteResult(true, Optional.empty());
    }

    @Override
    public @NotNull RouteResult leave(@NotNull Iterable<UUID> leavers) {
        for (UUID leaver : leavers) {
            if (!players.containsKey(leaver)) {
                return new RouteResult(false, Optional.of(Component.text("Not all players are within the scene.", NamedTextColor.RED)));
            }
        }

        for (UUID leaver : leavers) {
            players.remove(leaver);

            if (!stageTransition.getCurrentStage().hasPermanentPlayers()) {
                zombiesPlayers.remove(leaver);
            }
        }

        return new RouteResult(true, Optional.empty());
    }

    @Override
    public int getJoinWeight(@NotNull ZombiesJoinRequest request) {
        if (stageTransition.getCurrentStage().hasPermanentPlayers()) {
            return Integer.MIN_VALUE;
        }

        return 0;
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
    public void tick(long time) {
        super.tick(time);
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers.values()) {
            zombiesPlayer.tick(time);
        }
    }
}
