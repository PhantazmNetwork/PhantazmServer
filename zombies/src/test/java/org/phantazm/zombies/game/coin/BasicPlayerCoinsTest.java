package org.phantazm.zombies.game.coin;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.coin.BasicPlayerCoins;
import org.phantazm.zombies.coin.PlayerCoins;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.coin.TransactionResult;
import org.phantazm.zombies.coin.component.BasicTransactionComponentCreator;
import org.phantazm.zombies.coin.component.TransactionComponentCreator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BasicPlayerCoinsTest {

    private PlayerCoins coins;

    private void setup(int initialCoins) {
        PlayerView playerView = mock(PlayerView.class);
        Player player = mock(Player.class);
        when(playerView.getPlayer()).thenReturn(Optional.of(player));
        TransactionComponentCreator componentCreator = new BasicTransactionComponentCreator();

        coins = new BasicPlayerCoins(playerView, componentCreator, initialCoins);
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
        TransactionResult result = coins.runTransaction(new Transaction(Collections.emptyList(), delta));
        assertEquals(delta, result.change());
        assertEquals(0, coins.getCoins());
    }

    @Test
    public void testRemove() {
        setup(0);
        int delta = -10;
        TransactionResult result = coins.runTransaction(new Transaction(Collections.emptyList(), delta));
        assertEquals(delta, result.change());
    }

    @Test
    public void testOneModifier() {
        setup(0);
        int delta = 10;
        TransactionResult result =
                coins.runTransaction(new Transaction(Collections.singletonList(new Transaction.Modifier() {
                    @Override
                    public @NotNull Component getDisplayName() {
                        return Component.empty();
                    }

                    @Override
                    public int modify(int change) {
                        return change + delta;
                    }

                    @Override
                    public int getPriority() {
                        return 0;
                    }
                }), delta));
        assertEquals(delta * 2, result.change());
    }

    @Test
    public void testPriority() {
        setup(0);
        int delta = 10;
        Transaction.Modifier modifier1 = new Transaction.Modifier() {
            @Override
            public @NotNull Component getDisplayName() {
                return Component.empty();
            }

            @Override
            public int modify(int change) {
                return change + delta;
            }

            @Override
            public int getPriority() {
                return 0;
            }
        };
        int changedValue = 100;
        Transaction.Modifier modifier2 = new Transaction.Modifier() {
            @Override
            public @NotNull Component getDisplayName() {
                return Component.empty();
            }

            @Override
            public int modify(int change) {
                return changedValue;
            }

            @Override
            public int getPriority() {
                return 1;
            }
        };
        TransactionResult result = coins.runTransaction(new Transaction(List.of(modifier1, modifier2), delta));
        assertEquals(changedValue + delta, result.change());
    }

}
