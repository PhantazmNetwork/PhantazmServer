package org.phantazm.zombies.sidebar.lineupdater;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.player.state.PlayerStateSwitcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Model("zombies.sidebar.line_updater.player_state")
@Cache(false)
public class PlayerStateSidebarLineUpdater implements SidebarLineUpdater {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerStateSidebarLineUpdater.class);
    private static final int DISPLAY_REFRESH_INTERVAL = 2000;

    private final PlayerView playerView;

    private final PlayerStateSwitcher stateSwitcher;

    private CompletableFuture<? extends Component> playerNameFuture;

    private long lastDisplayRefresh;

    @FactoryMethod
    public PlayerStateSidebarLineUpdater(@NotNull PlayerView playerView, @NotNull PlayerStateSwitcher stateSwitcher) {
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.stateSwitcher = Objects.requireNonNull(stateSwitcher, "stateSwitcher");
    }

    @Override
    public void invalidateCache() {

    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
        if (time - lastDisplayRefresh >= DISPLAY_REFRESH_INTERVAL) {
            playerNameFuture = null;
            lastDisplayRefresh = time;
        }

        if (playerNameFuture == null) {
            playerNameFuture = playerView.getDisplayName();
        }

        try {
            Component playerName = playerNameFuture.getNow(null);
            if (playerName == null) {
                return Optional.empty();
            }

            return Optional.of(Component.textOfChildren(playerName, Component.text(": "),
                    stateSwitcher.getState().getDisplayName()));
        }
        catch (Throwable e) {
            LOGGER.warn("Error resolving player name", e);
            playerNameFuture = null;
        }

        return Optional.empty();
    }
}
