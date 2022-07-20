package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.lineupdater;

import com.github.phantazmnetwork.zombies.game.ZombiesScene;
import com.github.phantazmnetwork.zombies.game.player.state.PlayerStateSwitcher;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PlayerStateSidebarLineUpdater implements SidebarLineUpdater {

    private final PlayerStateSwitcher stateSwitcher;

    private Component playerName = null;

    public PlayerStateSidebarLineUpdater(@NotNull CompletableFuture<? extends Component> playerNameFuture,
                                         @NotNull PlayerStateSwitcher stateSwitcher) {
        this.stateSwitcher = Objects.requireNonNull(stateSwitcher, "stateSwitcher");
        playerNameFuture.thenAccept(playerName -> {
            this.playerName = playerName;
        });
    }

    @Override
    public void invalidateCache() {
        playerName = null;
    }

    @Override
    public @NotNull Optional<Component> tick(long time, @NotNull ZombiesScene scene) {
        if (playerName == null) {
            return Optional.empty();
        }

        return Optional.of(
                Component.textOfChildren(playerName, Component.text(": "), stateSwitcher.getState().getDisplayName()));
    }
}
