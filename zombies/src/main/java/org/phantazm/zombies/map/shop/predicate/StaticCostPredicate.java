package org.phantazm.zombies.map.shop.predicate;

import com.github.steanky.element.core.annotation.Cache;
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

@Model("zombies.map.shop.predicate.static_cost")
@Cache
public class StaticCostPredicate extends PredicateBase<StaticCostPredicate.Data> {
    @FactoryMethod
    public StaticCostPredicate(@NotNull Data data) {
        super(data);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction, @NotNull Shop shop) {
        TransactionModifierSource modifierSource = interaction.player().module().compositeTransactionModifiers();
        PlayerCoins coins = interaction.player().module().getCoins();
        return coins.runTransaction(new Transaction(modifierSource.modifiers(data.modifierType), -data.cost))
            .isAffordable(coins);
    }

    @DataObject
    public record Data(int cost,
        @NotNull Key modifierType) {
    }
}
