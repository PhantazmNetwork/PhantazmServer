package org.phantazm.zombies.map.shop.predicate;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.coin.PlayerCoins;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.coin.TransactionModifierSource;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

import java.util.Objects;

@Model("zombies.map.shop.predicate.static_cost")
@Cache(false)
public class StaticCostPredicate extends PredicateBase<StaticCostPredicate.Data> {
    private final TransactionModifierSource modifierSource;

    @FactoryMethod
    public StaticCostPredicate(@NotNull Data data, @NotNull TransactionModifierSource modifierSource) {
        super(data);
        this.modifierSource = Objects.requireNonNull(modifierSource);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction, @NotNull Shop shop) {
        TransactionModifierSource modifierSource = data.target == Target.SCENE ? this.modifierSource :
            interaction.player().module().playerTransactionModifiers();

        PlayerCoins coins = interaction.player().module().getCoins();
        return coins.runTransaction(new Transaction(modifierSource.modifiers(data.modifierType), -data.cost))
            .isAffordable(coins);
    }

    public enum Target {
        PLAYER,
        SCENE
    }

    @Default("""
        {
          target='SCENE'
        }
        """)
    @DataObject
    public record Data(int cost,
        @NotNull Key modifierType,
        @NotNull Target target) {
    }
}
