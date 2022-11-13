package com.github.phantazmnetwork.zombies.player.state.revive;

import com.github.phantazmnetwork.commons.Activable;
import com.github.phantazmnetwork.commons.Wrapper;
import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.core.time.TickFormatter;
import com.github.phantazmnetwork.zombies.listener.PlayerMoveCancelListener;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayerMeta;
import com.github.phantazmnetwork.zombies.player.state.context.KnockedPlayerStateContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BasicKnockedStateActivable implements Activable {

    private final KnockedPlayerStateContext context;

    private final Instance instance;

    private final EventNode<? super PlayerMoveEvent> eventNode;

    private final PlayerView playerView;

    private final ReviveHandler reviveHandler;

    private final TickFormatter tickFormatter;

    private final Sidebar sidebar;

    private final ZombiesPlayerMeta meta;

    private final EventListener<PlayerMoveEvent> moveEventListener;

    public BasicKnockedStateActivable(@NotNull KnockedPlayerStateContext context, @NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers,
            @NotNull EventNode<? super PlayerMoveEvent> eventNode, @NotNull PlayerView playerView,
            @NotNull ReviveHandler reviveHandler, @NotNull TickFormatter tickFormatter, @NotNull ZombiesPlayerMeta meta,
            @NotNull Sidebar sidebar) {
        this.context = Objects.requireNonNull(context, "context");
        this.instance = Objects.requireNonNull(instance, "instance");
        this.eventNode = Objects.requireNonNull(eventNode, "eventNode");
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.reviveHandler = Objects.requireNonNull(reviveHandler, "reviveHandler");
        this.tickFormatter = Objects.requireNonNull(tickFormatter, "tickFormatter");
        this.meta = Objects.requireNonNull(meta, "meta");
        this.sidebar = Objects.requireNonNull(sidebar, "sidebar");
        this.moveEventListener =
                EventListener.of(PlayerMoveEvent.class, new PlayerMoveCancelListener(instance, zombiesPlayers));
    }

    @Override
    public void start() {
        playerView.getPlayer().ifPresent(player -> {
            player.setInvisible(true);
            player.setAllowFlying(false);
            player.setFlying(false);
            player.setGameMode(GameMode.ADVENTURE);
            sidebar.addViewer(player);
        });
        playerView.getDisplayName().thenAccept(displayName -> {
            instance.sendMessage(buildDeathMessage(displayName));
        });
        eventNode.addListener(moveEventListener);
        meta.setInGame(true);
        meta.setCanRevive(false);
        meta.setCanTriggerSLA(false);
    }

    @Override
    public void tick(long time) {
        reviveHandler.getReviver().ifPresentOrElse(reviver -> {
            Wrapper<Component> knockedDisplayName = Wrapper.ofNull(), reviverDisplayName = Wrapper.ofNull();
            reviver.getModule().getPlayerView().getDisplayName().thenAccept(reviverDisplayName::set);
            CompletableFuture.allOf(playerView.getDisplayName().thenAccept(knockedDisplayName::set),
                            reviver.getModule().getPlayerView().getDisplayName().thenAccept(reviverDisplayName::set))
                    .thenAccept(v -> {
                        sendReviveStatus(reviver, knockedDisplayName.get(), reviverDisplayName.get());
                    });
        }, this::sendDyingStatus);
    }

    @Override
    public void end() {
        playerView.getPlayer().ifPresent(player -> {
            player.setInvisible(false);
            player.setAllowFlying(false);
            player.setFlying(false);
            player.setGameMode(GameMode.ADVENTURE);
            sidebar.addViewer(player);
        });
        eventNode.removeListener(moveEventListener);
    }

    private void sendReviveStatus(@NotNull ZombiesPlayer reviver, @NotNull Component knockedDisplayName,
            @NotNull Component reviverDisplayName) {
        reviver.getModule().getPlayerView().getPlayer().ifPresent(reviverPlayer -> {
            reviverPlayer.sendActionBar(
                    Component.textOfChildren(Component.text("Reviving "), knockedDisplayName, Component.text(" - "),
                            tickFormatter.format(reviveHandler.getTicksUntilRevive())));
        });
        playerView.getPlayer().ifPresent(player -> {
            player.sendActionBar(Component.textOfChildren(reviverDisplayName, Component.text(" is reviving you - "),
                    tickFormatter.format(reviveHandler.getTicksUntilRevive())));
        });
    }

    private void sendDyingStatus() {
        playerView.getPlayer().ifPresent(player -> {
            player.sendActionBar(Component.textOfChildren(Component.text("You are dying - "),
                    tickFormatter.format(reviveHandler.getTicksUntilRevive())));
        });
    }

    private @NotNull Component buildDeathMessage(@NotNull Component displayName) {
        TextComponent.Builder builder = Component.text();
        builder.append(displayName);
        builder.append(Component.text(" was knocked down"));
        context.getKnockRoom().ifPresent(room -> {
            builder.append(Component.text(" in "));
            builder.append(room);
        });
        context.getKiller().ifPresent(killer -> {
            builder.append(Component.text(" by "));
            builder.append(killer);
        });
        builder.append(Component.text("."));

        return builder.build();
    }
}
