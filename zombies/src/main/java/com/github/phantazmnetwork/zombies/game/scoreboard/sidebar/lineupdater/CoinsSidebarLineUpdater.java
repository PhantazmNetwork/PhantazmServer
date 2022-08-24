package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.lineupdater;

import com.github.phantazmnetwork.zombies.game.coin.PlayerCoins;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class CoinsSidebarLineUpdater implements SidebarLineUpdater {

    private final PlayerCoins coins;

    private Component playerName = null;

    private int lastCoins = -1;

    public CoinsSidebarLineUpdater(@NotNull CompletableFuture<? extends Component> playerNameFuture,
            @NotNull PlayerCoins coins) {
        this.coins = Objects.requireNonNull(coins, "coins");
        playerNameFuture.thenAccept(playerName -> {
            this.playerName = playerName;
        });
    }

    @Override
    public void invalidateCache() {
        lastCoins = -1;
    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
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
