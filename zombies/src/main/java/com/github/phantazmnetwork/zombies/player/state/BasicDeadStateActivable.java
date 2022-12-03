package com.github.phantazmnetwork.zombies.player.state;

import com.github.phantazmnetwork.commons.Activable;
import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayerMeta;
import com.github.phantazmnetwork.zombies.player.state.context.DeadPlayerStateContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.Instance;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BasicDeadStateActivable implements Activable {

    private final DeadPlayerStateContext context;

    private final Instance instance;

    private final PlayerView playerView;

    private final ZombiesPlayerMeta meta;

    private final Sidebar sidebar;

    public BasicDeadStateActivable(@NotNull DeadPlayerStateContext context, @NotNull Instance instance,
            @NotNull PlayerView playerView, @NotNull ZombiesPlayerMeta meta, @NotNull Sidebar sidebar) {
        this.context = Objects.requireNonNull(context, "context");
        this.instance = Objects.requireNonNull(instance, "instance");
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.meta = Objects.requireNonNull(meta, "meta");
        this.sidebar = Objects.requireNonNull(sidebar, "sidebar");
    }

    @Override
    public void start() {
        playerView.getPlayer().ifPresent(player -> {
            player.setInvisible(true);
            player.setAllowFlying(true);
            player.setFlying(true);
            player.setGameMode(GameMode.ADVENTURE);
            sidebar.addViewer(player);
        });
        playerView.getDisplayName().thenAccept(displayName -> {
            instance.sendMessage(buildDeathMessage(displayName));
        });
        meta.setInGame(true);
        meta.setCanRevive(false);
        meta.setCanTriggerSLA(false);
    }

    @Override
    public void end() {
        playerView.getPlayer().ifPresent(player -> {
            player.setInvisible(false);
            player.setFlying(false);
            player.setAllowFlying(false);
            player.setGameMode(GameMode.ADVENTURE);
            sidebar.addViewer(player);
        });
    }

    private @NotNull Component buildDeathMessage(@NotNull Component displayName) {
        if (context.isRejoin()) {
            return Component.textOfChildren(displayName, Component.text(" rejoined."));
        }

        TextComponent.Builder builder = Component.text();
        builder.append(displayName);
        builder.append(Component.text(" was killed"));
        context.getDeathRoomName().ifPresent(deathRoomName -> {
            builder.append(Component.text(" in "));
            builder.append(deathRoomName);
        });
        context.getKiller().ifPresent(killer -> {
            builder.append(Component.text(" by "));
            builder.append(killer);
        });
        builder.append(Component.text("."));

        return builder.build();
    }

}
