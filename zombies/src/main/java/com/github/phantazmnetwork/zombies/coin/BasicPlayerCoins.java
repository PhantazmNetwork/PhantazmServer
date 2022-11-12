package com.github.phantazmnetwork.zombies.coin;

import com.github.phantazmnetwork.zombies.audience.ComponentSender;
import com.github.phantazmnetwork.zombies.equipment.gun.audience.AudienceProvider;
import com.github.phantazmnetwork.zombies.coin.component.TransactionComponentCreator;
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
    public @NotNull TransactionResult runTransaction(@NotNull Transaction transaction) {
        List<Transaction.Modifier> modifiers = new ArrayList<>(transaction.modifiers());
        modifiers.sort(Comparator.comparingInt(Transaction.Modifier::getPriority).reversed());

        List<Component> modifierNames = new ArrayList<>(modifiers.size());
        int change = transaction.initialChange();
        for (Transaction.Modifier modifier : modifiers) {
            int newChange = modifier.modify(change);
            if (change != newChange) {
                modifierNames.add(modifier.getDisplayName());
            }

            change = newChange;
        }

        int coinsUntilMax = Integer.MAX_VALUE - coins;
        if (coinsUntilMax < change) {
            change = coinsUntilMax;
        }
        else {
            int coinsUntilMin = Integer.MIN_VALUE - coins;
            if (coinsUntilMin > change) {
                change = coinsUntilMin;
            }
        }

        return new TransactionResult(modifierNames, change);
    }

    @Override
    public int getCoins() {
        return coins;
    }

    @Override
    public void applyTransaction(@NotNull TransactionResult result) {
        if (result.change() == 0) {
            return;
        }

        coins += result.change();
        audienceProvider.provideAudience().ifPresent(audience -> {
            Component message = componentCreator.createTransactionComponent(result.modifierNames(), result.change());
            componentSender.send(audience, message);
        });
    }
}
