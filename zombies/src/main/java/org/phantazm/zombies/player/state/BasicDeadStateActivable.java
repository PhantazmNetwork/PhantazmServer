package org.phantazm.zombies.player.state;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.Instance;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Activable;
import org.phantazm.core.inventory.InventoryAccessRegistry;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.player.ZombiesPlayerMeta;
import org.phantazm.zombies.player.state.context.DeadPlayerStateContext;

import java.util.Objects;

public class BasicDeadStateActivable implements Activable {
    private final InventoryAccessRegistry accessRegistry;
    private final DeadPlayerStateContext context;
    private final Instance instance;
    private final PlayerView playerView;
    private final ZombiesPlayerMeta meta;
    private final Sidebar sidebar;

    public BasicDeadStateActivable(@NotNull InventoryAccessRegistry accessRegistry,
            @NotNull DeadPlayerStateContext context, @NotNull Instance instance, @NotNull PlayerView playerView,
            @NotNull ZombiesPlayerMeta meta, @NotNull Sidebar sidebar) {
        this.accessRegistry = Objects.requireNonNull(accessRegistry, "accessRegistry");
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
            player.setGameMode(GameMode.SPECTATOR);
            sidebar.addViewer(player);
        });
        playerView.getDisplayName().thenAccept(displayName -> {
            instance.sendMessage(buildDeathMessage(displayName));
        });

        accessRegistry.switchAccess(InventoryKeys.DEAD_ACCESS);

        meta.setInGame(true);
        meta.setCanRevive(false);
        meta.setCanTriggerSLA(false);
    }

    @Override
    public void end() {
        playerView.getPlayer().ifPresent(player -> {
            player.setInvisible(false);
            player.setGameMode(GameMode.ADVENTURE);
            sidebar.addViewer(player);
        });

        accessRegistry.switchAccess(null);
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
