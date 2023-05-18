package org.phantazm.zombies.sidebar.lineupdater.creator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.coin.PlayerCoins;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.ZombiesPlayerModule;
import org.phantazm.zombies.sidebar.lineupdater.SidebarLineUpdater;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Model("zombies.sidebar.line_updater.creator.coins")
@Cache(false)
public class CoinsUpdaterCreator implements PlayerUpdaterCreator {
    private final Data data;

    @FactoryMethod
    public CoinsUpdaterCreator(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull SidebarLineUpdater forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        ZombiesPlayerModule module = zombiesPlayer.module();
        return new Updater(data, module.getPlayerView(), module.getCoins());
    }

    private static class Updater implements SidebarLineUpdater {
        private final Data data;
        private final PlayerView playerView;
        private final PlayerCoins coins;

        private CompletableFuture<String> playerNameFuture;
        private int lastCoins;
        private boolean cacheInvalidated;

        private Updater(@NotNull Data data, @NotNull PlayerView playerView, @NotNull PlayerCoins coins) {
            this.data = Objects.requireNonNull(data, "data");
            this.playerView = Objects.requireNonNull(playerView, "playerView");
            this.coins = Objects.requireNonNull(coins, "coins");
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

            int newCoins = coins.getCoins();
            if (cacheInvalidated || lastCoins != newCoins) {
                lastCoins = newCoins;
                cacheInvalidated = false;

                return Optional.of(
                        MiniMessage.miniMessage().deserialize(String.format(data.formatString, playerName, newCoins)));
            }

            return Optional.empty();
        }
    }

    @DataObject
    public record Data(@NotNull String formatString) {
    }
}
