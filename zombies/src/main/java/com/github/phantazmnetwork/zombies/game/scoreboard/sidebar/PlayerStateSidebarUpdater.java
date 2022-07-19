package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar;

import com.github.phantazmnetwork.zombies.game.ZombiesSceneState;
import com.github.phantazmnetwork.zombies.game.player.state.PlayerStateSwitcher;
import com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.linechooser.SidebarLineChooser;
import net.kyori.adventure.text.Component;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class PlayerStateSidebarUpdater extends SidebarUpdaterBase {

    private final PlayerStateSwitcher stateSwitcher;

    private Component playerName = null;

    public PlayerStateSidebarUpdater(@NotNull String lineName, @NotNull SidebarLineChooser lineChooser,
                                     @NotNull CompletableFuture<? extends Component> playerNameFuture,
                                     @NotNull PlayerStateSwitcher stateSwitcher) {
        super(lineName, lineChooser);
        this.stateSwitcher = Objects.requireNonNull(stateSwitcher, "stateSwitcher");
        playerNameFuture.thenAccept(playerName -> {
            this.playerName = playerName;
        });
    }

    public PlayerStateSidebarUpdater(@NotNull SidebarLineChooser lineChooser,
                                     @NotNull CompletableFuture<? extends Component> playerNameFuture,
                                     @NotNull PlayerStateSwitcher stateSwitcher) {
        this("zombie-kills-display", lineChooser, playerNameFuture, stateSwitcher);
    }

    @Override
    public void tick(@NotNull ZombiesSceneState state, @NotNull Sidebar sidebar) {
        if (playerName == null) {
            return;
        }

        Component message =
                Component.textOfChildren(playerName, Component.text(": "), stateSwitcher.getState().getDisplayName());
        sidebar.updateLineContent(getLineName(), message);
    }
}
