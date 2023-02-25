package org.phantazm.zombies.player.state;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Activable;
import org.phantazm.core.inventory.InventoryAccessRegistry;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.player.ZombiesPlayerMeta;

import java.util.Objects;

public class BasicAliveStateActivable implements Activable {
    private final InventoryAccessRegistry accessRegistry;
    private final PlayerView playerView;
    private final ZombiesPlayerMeta meta;
    private final Sidebar sidebar;

    private long lastHeal;

    public BasicAliveStateActivable(@NotNull InventoryAccessRegistry accessRegistry, @NotNull PlayerView playerView,
            @NotNull ZombiesPlayerMeta meta, @NotNull Sidebar sidebar) {
        this.accessRegistry = Objects.requireNonNull(accessRegistry, "accessRegistry");
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.meta = Objects.requireNonNull(meta, "meta");
        this.sidebar = Objects.requireNonNull(sidebar, "sidebar");
    }

    @Override
    public void start() {
        playerView.getPlayer().ifPresent(player -> {
            player.setInvisible(false);
            player.setAllowFlying(false);
            player.setFlying(false);
            player.setGameMode(GameMode.ADVENTURE);
            sidebar.addViewer(player);
        });

        accessRegistry.switchAccess(InventoryKeys.ALIVE_ACCESS);

        meta.setInGame(true);
        meta.setCanRevive(true);
        meta.setCanTriggerSLA(true);
    }

    @Override
    public void tick(long time) {
        if ((time - lastHeal) / MinecraftServer.TICK_MS >= meta.getTicksPerHeal()) {
            playerView.getPlayer().ifPresent(player -> {
                player.setHealth(player.getHealth() + 0.5F);
            });

            lastHeal = time;
        }
    }

    @Override
    public void end() {
        playerView.getPlayer().ifPresent(player -> {
            player.setInvisible(false);
            player.setAllowFlying(false);
            player.setFlying(false);
            player.setGameMode(GameMode.ADVENTURE);
            player.clearEffects();
            sidebar.addViewer(player);
        });

        accessRegistry.switchAccess(null);
    }
}
