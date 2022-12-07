package com.github.phantazmnetwork.zombies.map.shop.predicate;

import com.github.phantazmnetwork.zombies.coin.PlayerCoins;
import com.github.phantazmnetwork.zombies.coin.Transaction;
import com.github.phantazmnetwork.zombies.coin.TransactionModifierSource;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.upgrade.Upgradable;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Model("zombies.map.shop.predicate.equipment_cost")
public class EquipmentCostPredicate extends PredicateBase<EquipmentCostPredicate.Data> {
    @FactoryMethod
    public EquipmentCostPredicate(@NotNull Data data) {
        super(data);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        ZombiesPlayer player = interaction.player();

        PlayerCoins coins = player.getModule().getCoins();
        Optional<Equipment> equipmentOptional = player.getHeldEquipment();
        TransactionModifierSource modifierSource = player.getModule().compositeTransactionModifiers();
        if (equipmentOptional.isEmpty()) {
            return coins.runTransaction(
                            new Transaction(modifierSource.modifiers(data.modifierType), -data.purchaseCost))
                    .isAffordable(coins);
        }

        Equipment equipment = equipmentOptional.get();
        if (equipment instanceof Upgradable upgradable) {
            Set<Key> upgradeKeys = upgradable.getSuggestedUpgrades();
            if (upgradeKeys.isEmpty()) {
                return false;
            }

            for (Key upgradeKey : upgradeKeys) {
                if (data.upgradeCosts.containsKey(upgradeKey)) {
                    int cost = data.upgradeCosts.getInt(upgradeKey);
                    return coins.runTransaction(new Transaction(modifierSource.modifiers(data.modifierType), -cost))
                            .isAffordable(coins);
                }
            }
        }

        return false;
    }

    @DataObject
    public record Data(int purchaseCost, @NotNull Key modifierType, @NotNull Object2IntMap<Key> upgradeCosts) {
    }
}
