package org.phantazm.zombies.sidebar.lineupdater.creator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.PlayerStateSwitcher;
import org.phantazm.zombies.player.state.ZombiesPlayerState;
import org.phantazm.zombies.sidebar.lineupdater.SidebarLineUpdater;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Model("zombies.sidebar.line_updater.creator.state")
@Cache(false)
public class StateUpdaterCreator implements PlayerUpdaterCreator {
    private final Data data;

    @FactoryMethod
    public StateUpdaterCreator(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull SidebarLineUpdater forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        return new Updater(data, zombiesPlayer.module().getPlayerView(), zombiesPlayer.module().getStateSwitcher());
    }

    private static class Updater implements SidebarLineUpdater {
        private final Data data;
        private final PlayerView playerView;
        private final PlayerStateSwitcher stateSwitcher;

        private CompletableFuture<? extends String> playerNameFuture;
        private ZombiesPlayerState lastState;
        private boolean cacheInvalidated;

        @FactoryMethod
        public Updater(@NotNull Data data, @NotNull PlayerView playerView, @NotNull PlayerStateSwitcher stateSwitcher) {
            this.data = Objects.requireNonNull(data, "data");
            this.playerView = Objects.requireNonNull(playerView, "playerView");
            this.stateSwitcher = Objects.requireNonNull(stateSwitcher, "stateSwitcher");
            this.cacheInvalidated = true;
        }

        @Override
        public void invalidateCache() {
            cacheInvalidated = true;
            playerNameFuture = null;
        }

        @Override
        public @NotNull Optional<Component> tick(long time) {
            if (playerNameFuture == null) {
                playerNameFuture = playerView.getUsername();
            }

            if (!playerNameFuture.isDone()) {
                return Optional.empty();
            }

            String playerName = playerNameFuture.getNow(null);
            if (playerName == null) {
                return Optional.empty();
            }

            ZombiesPlayerState currentState = stateSwitcher.getState();
            if (cacheInvalidated || currentState != lastState) {
                this.lastState = currentState;
                this.cacheInvalidated = false;

                Component first = MiniMessage.miniMessage().deserialize(String.format(data.formatString, playerName));
                return Optional.of(
                        Component.join(JoinConfiguration.noSeparators(), first, currentState.getDisplayName()));
            }

            return Optional.empty();
        }
    }

    @DataObject
    public record Data(@NotNull String formatString) {
    }
}
