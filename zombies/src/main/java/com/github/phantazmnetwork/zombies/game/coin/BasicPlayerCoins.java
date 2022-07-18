package com.github.phantazmnetwork.zombies.game.coin;

import com.github.phantazmnetwork.zombies.audience.ComponentSender;
import com.github.phantazmnetwork.zombies.equipment.gun.audience.AudienceProvider;
import com.github.phantazmnetwork.zombies.game.coin.component.TransactionComponentCreator;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class BasicPlayerCoins implements PlayerCoins {

    private final AudienceProvider audienceProvider;

    private final ComponentSender componentSender;

    private final TransactionComponentCreator componentCreator;

    private int coins;

    public BasicPlayerCoins(@NotNull AudienceProvider audienceProvider, @NotNull ComponentSender componentSender,
                            @NotNull TransactionComponentCreator componentCreator, int initialCoins) {
        this.audienceProvider = Objects.requireNonNull(audienceProvider, "audienceProvider");
        this.componentSender = Objects.requireNonNull(componentSender, "componentSender");
        this.componentCreator = Objects.requireNonNull(componentCreator, "componentCreator");
        this.coins = initialCoins;
    }

    @Override
    public void runTransaction(@NotNull Transaction transaction) {
        List<Transaction.Modifier> modifiers = new ArrayList<>(transaction.modifiers());
        modifiers.sort(Comparator.comparingInt(Transaction.Modifier::getPriority).reversed());

        int change = transaction.initialChange();
        for (Transaction.Modifier modifier : modifiers) {
            change = modifier.modify(change);
        }

        coins += change;
        if (change != 0) {
            int finalChange = change;
            audienceProvider.provideAudience().ifPresent(audience -> {
                Component message = componentCreator.createTransactionComponent(modifiers, finalChange);
                componentSender.send(audience, message);
            });
        }
    }

    @Override
    public int getCoins() {
        return coins;
    }
}
