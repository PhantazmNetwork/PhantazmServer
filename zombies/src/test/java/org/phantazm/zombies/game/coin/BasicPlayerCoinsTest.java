package org.phantazm.zombies.game.coin;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.entity.Player;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.phantazm.core.player.PlayerView;
import org.phantazm.stats.zombies.BasicZombiesPlayerMapStats;
import org.phantazm.zombies.coin.BasicPlayerCoins;
import org.phantazm.zombies.coin.PlayerCoins;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.coin.TransactionResult;
import org.phantazm.zombies.coin.component.BasicTransactionMessager;
import org.phantazm.zombies.coin.component.TransactionMessager;
import org.phantazm.zombies.map.PlayerCoinsInfo;
import org.phantazm.zombies.player.action_bar.ZombiesPlayerActionBar;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Disabled("because mocks don't work on sealed interfaces")
public class BasicPlayerCoinsTest {

    private PlayerCoins coins;

    private void setup(int initialCoins) {
        PlayerView playerView = PlayerView.lookup(UUID.randomUUID());
        Player player = mock(Player.class);
        when(playerView.getPlayer()).thenReturn(Optional.of(player));
        ZombiesPlayerActionBar actionBar = new ZombiesPlayerActionBar(playerView);
        TransactionMessager componentCreator = new BasicTransactionMessager(actionBar, MiniMessage.miniMessage(),
            new PlayerCoinsInfo("", "", NamedTextColor.WHITE, NamedTextColor.WHITE, 20L));

        coins = new BasicPlayerCoins(
            BasicZombiesPlayerMapStats.createBasicStats(UUID.randomUUID(), Key.key("phantazm:test")),
            componentCreator, initialCoins);
    }

    @Test
    public void testRespectInitialCoins() {
        int initialCoins = 10;
        setup(initialCoins);
        assertEquals(initialCoins, coins.getCoins());
    }

    @Test
    public void testAdd() {
        setup(0);
        int delta = 10;
        TransactionResult result = coins.runTransaction(new Transaction(List.of(), delta));
        assertEquals(delta, result.change());
        assertEquals(0, coins.getCoins());
    }

    @Test
    public void testRemove() {
        setup(0);
        int delta = -10;
        TransactionResult result = coins.runTransaction(new Transaction(List.of(), delta));
        assertEquals(delta, result.change());
    }

    @Test
    public void testOneModifier() {
        setup(0);
        int delta = 10;
        TransactionResult result = coins.runTransaction(new Transaction(
            List.of(Transaction.modifier(Component.empty(), Transaction.Modifier.Action.ADD, delta)), delta));
        assertEquals(delta * 2, result.change());
    }

    @Test
    public void testPriority() {
        setup(0);
        int delta = 10;
        Transaction.Modifier modifier1 =
            Transaction.modifier(Component.empty(), Transaction.Modifier.Action.ADD, delta);
        int changedValue = 100;
        Transaction.Modifier modifier2 =
            Transaction.modifier(Component.empty(), Transaction.Modifier.Action.SET, changedValue, 1);
        TransactionResult result = coins.runTransaction(new Transaction(List.of(modifier1, modifier2), delta));
        assertEquals(changedValue + delta, result.change());
    }

}
