package org.phantazm.zombies.scoreboard.sidebar.lineupdater;

import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.coin.PlayerCoins;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Model("zombies.sidebar.lineupdater.coins")
public class CoinsSidebarLineUpdater implements SidebarLineUpdater {

    private final CompletableFuture<? extends Component> playerNameFuture;

    private final PlayerCoins coins;

    private int lastCoins = -1;

    @FactoryMethod
    public CoinsSidebarLineUpdater(@NotNull PlayerView playerView, @NotNull PlayerCoins coins) {
        this.playerNameFuture = playerView.getDisplayName();
        this.coins = Objects.requireNonNull(coins, "coins");
    }

    @Override
    public void invalidateCache() {
        lastCoins = -1;
    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
        Component playerName = playerNameFuture.getNow(null);
        if (playerName == null) {
            return Optional.empty();
        }

        int newCoins = coins.getCoins();
        if (lastCoins == -1 || lastCoins != newCoins) {
            lastCoins = newCoins;
            return Optional.of(Component.textOfChildren(playerName, Component.text(": "),
                    Component.text(coins.getCoins(), NamedTextColor.GOLD)));
        }

        return Optional.empty();
    }
}