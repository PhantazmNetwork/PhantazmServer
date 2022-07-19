package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar;

import com.github.phantazmnetwork.zombies.game.ZombiesScene;
import com.github.phantazmnetwork.zombies.game.coin.PlayerCoins;
import com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.linechooser.SidebarLineChooser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class CoinsSidebarUpdater extends SidebarUpdaterBase {

    private final PlayerCoins coins;

    private Component playerName = null;

    public CoinsSidebarUpdater(@NotNull String lineName, @NotNull SidebarLineChooser lineChooser,
                               @NotNull CompletableFuture<? extends Component> playerNameFuture,
                               @NotNull PlayerCoins coins) {
        super(lineName, lineChooser);
        this.coins = Objects.requireNonNull(coins, "coins");
        playerNameFuture.thenAccept(playerName -> {
            this.playerName = playerName;
        });
    }

    public CoinsSidebarUpdater(@NotNull SidebarLineChooser lineChooser,
                               @NotNull CompletableFuture<? extends Component> playerNameFuture,
                               @NotNull PlayerCoins coins) {
        this("coins-display", lineChooser, playerNameFuture, coins);
    }

    @Override
    public void tick(@NotNull ZombiesScene scene, @NotNull Sidebar sidebar) {
        if (playerName == null) {
            return;
        }

        Component message = Component.textOfChildren(playerName, Component.text(": "),
                                                     Component.text(coins.getCoins(), NamedTextColor.GOLD)
        );
        sidebar.updateLineContent(getLineName(), message);
    }
}
