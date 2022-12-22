package org.phantazm.zombies.scoreboard.sidebar.lineupdater;

import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.player.state.PlayerStateSwitcher;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Model("zombies.sidebar.lineupdater.player_state")
public class PlayerStateSidebarLineUpdater implements SidebarLineUpdater {

    private final CompletableFuture<? extends Component> playerNameFuture;

    private final PlayerStateSwitcher stateSwitcher;

    @FactoryMethod
    public PlayerStateSidebarLineUpdater(
            @NotNull @Dependency("zombies.dependency.player.player_view") PlayerView playerView,
            @NotNull @Dependency("zombies.dependency.player.state_switcher") PlayerStateSwitcher stateSwitcher) {
        this.playerNameFuture = playerView.getDisplayName();
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
