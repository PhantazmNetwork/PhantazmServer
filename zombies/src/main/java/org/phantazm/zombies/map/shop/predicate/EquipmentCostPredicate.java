package org.phantazm.zombies.map.shop.predicate;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.Upgradable;
import org.phantazm.zombies.coin.PlayerCoins;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.coin.TransactionModifierSource;
import org.phantazm.zombies.coin.TransactionResult;
import org.phantazm.zombies.map.shop.PlayerInteraction;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Model("zombies.map.shop.equipment_predicate.upgrade_cost")
@Cache(false)
public class EquipmentCostPredicate implements EquipmentPredicate {
    private final Data data;
    private final TransactionModifierSource transactionModifierSource;

    @FactoryMethod
    public EquipmentCostPredicate(@NotNull Data data, @NotNull TransactionModifierSource transactionModifierSource) {
        this.data = Objects.requireNonNull(data, "data");
        this.transactionModifierSource = Objects.requireNonNull(transactionModifierSource, "transactionModifierSource");
    }

    @Override
    public boolean canUpgrade(@NotNull PlayerInteraction playerInteraction, @NotNull Upgradable upgradeTarget,
            @NotNull Key chosenUpgrade) {
        PlayerCoins coins = playerInteraction.player().module().getCoins();

        Integer cost = data.upgradeCosts.get(chosenUpgrade);
        if (cost == null) {
            return false;
        }

        TransactionResult result =
                coins.runTransaction(new Transaction(transactionModifierSource.modifiers(data.modifier),
                        Collections.emptyList(), -cost));
        return result.isAffordable(coins);
    }

    @Override
    public boolean canAdd(@NotNull PlayerInteraction playerInteraction, @NotNull Key equipmentType) {
        PlayerCoins coins = playerInteraction.player().module().getCoins();

        return coins.runTransaction(new Transaction(transactionModifierSource.modifiers(data.modifier), -data.baseCost))
                .isAffordable(coins);
    }

    @DataObject
    public record Data(int baseCost, @NotNull Map<Key, Integer> upgradeCosts, @NotNull Key modifier) {
    }
}
