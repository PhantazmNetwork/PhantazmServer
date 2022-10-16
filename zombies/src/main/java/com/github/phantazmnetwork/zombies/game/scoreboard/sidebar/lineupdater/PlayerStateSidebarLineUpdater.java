package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.lineupdater;

import com.github.phantazmnetwork.zombies.game.player.state.PlayerStateSwitcher;
import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Model("zombies.sidebar.lineupdater.player_state")
public class PlayerStateSidebarLineUpdater implements SidebarLineUpdater {

    private final CompletableFuture<? extends Component> playerNameFuture;

    private final PlayerStateSwitcher stateSwitcher;

    @FactoryMethod
    public PlayerStateSidebarLineUpdater(@NotNull @Dependency("zombies.dependency.player.name_future")
    CompletableFuture<? extends Component> playerNameFuture,
            @NotNull @Dependency("zombies.dependency.player.state_switcher") PlayerStateSwitcher stateSwitcher) {
        this.playerNameFuture = Objects.requireNonNull(playerNameFuture, "playerNameFuture");
        this.stateSwitcher = Objects.requireNonNull(stateSwitcher, "stateSwitcher");
    }

    @Override
    public void invalidateCache() {

    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
        Component playerName = playerNameFuture.getNow(null);
        if (playerName == null) {
            return Optional.empty();
        }

        return Optional.of(
                Component.textOfChildren(playerName, Component.text(": "), stateSwitcher.getState().getDisplayName()));
    }
}
