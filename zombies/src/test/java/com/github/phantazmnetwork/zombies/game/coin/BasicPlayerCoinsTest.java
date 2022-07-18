package com.github.phantazmnetwork.zombies.game.coin;

import com.github.phantazmnetwork.zombies.audience.ComponentSender;
import com.github.phantazmnetwork.zombies.equipment.gun.audience.AudienceProvider;
import com.github.phantazmnetwork.zombies.game.coin.component.TransactionComponentCreator;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BasicPlayerCoinsTest {

    private PlayerCoins coins;

    private ComponentSender componentSender;

    private void setup(int initialCoins) {
        Audience audience = mock(Audience.class);
        AudienceProvider audienceProvider = () -> Optional.of(audience);
        componentSender = mock(ComponentSender.class);
        TransactionComponentCreator componentCreator = mock(TransactionComponentCreator.class);

        coins = new BasicPlayerCoins(audienceProvider, componentSender, componentCreator, initialCoins);
    }

    @Test
    public void testAdd() {
        setup(0);
        int delta = 10;
        coins.runTransaction(new Transaction(Collections.emptyList(), delta), true);
        assertEquals(delta, coins.getCoins());
    }

    @Test
    public void testRemove() {
        setup(0);
        int delta = -10;
        coins.runTransaction(new Transaction(Collections.emptyList(), delta), true);
        assertEquals(delta, coins.getCoins());
    }

    @Test
    public void testRespectInitialCoins() {
        int initialCoins = 10;
        setup(initialCoins);
        assertEquals(initialCoins, coins.getCoins());

        int delta = 10;
        coins.runTransaction(new Transaction(Collections.emptyList(), delta), true);
        assertEquals(initialCoins + delta, coins.getCoins());
    }

    @Test
    public void testOneModifier() {
        setup(0);
        int delta = 10;
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
        }), delta), true);
        assertEquals(delta * 2, coins.getCoins());
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
        coins.runTransaction(new Transaction(List.of(modifier1, modifier2), delta), true);
        assertEquals(changedValue + delta, coins.getCoins());
    }

    @Test
    public void testNotSilent() {
        setup(0);
        int delta = 10;
        coins.runTransaction(new Transaction(Collections.emptyList(), delta), false);
        verify(componentSender, times(1)).send(any(), any());
    }

}
