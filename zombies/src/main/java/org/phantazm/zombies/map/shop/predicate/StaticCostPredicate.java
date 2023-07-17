package org.phantazm.zombies.map.shop.predicate;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.coin.PlayerCoins;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.coin.TransactionModifierSource;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

import java.util.Objects;

@Model("zombies.map.shop.predicate.static_cost")
public class StaticCostPredicate extends PredicateBase<StaticCostPredicate.Data> {
    private final TransactionModifierSource transactionModifierSource;

    @FactoryMethod
    public StaticCostPredicate(@NotNull Data data, @NotNull TransactionModifierSource transactionModifierSource) {
        super(data);
        this.transactionModifierSource = Objects.requireNonNull(transactionModifierSource, "modifierSource");
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction, @NotNull Shop shop) {
        PlayerCoins coins = interaction.player().module().getCoins();
        return coins.runTransaction(new Transaction(transactionModifierSource.modifiers(data.modifierType), -data.cost))
                .isAffordable(coins);
    }

    @DataObject
    public record Data(int cost, @NotNull Key modifierType) {
    }
}
