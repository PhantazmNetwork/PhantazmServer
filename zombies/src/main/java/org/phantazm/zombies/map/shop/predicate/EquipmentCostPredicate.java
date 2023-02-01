package org.phantazm.zombies.map.shop.predicate;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.coin.PlayerCoins;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.coin.TransactionModifierSource;
import org.phantazm.zombies.equipment.Equipment;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.upgrade.Upgradable;

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

        PlayerCoins coins = player.module().getCoins();
        Optional<Equipment> equipmentOptional = player.getHeldEquipment();
        TransactionModifierSource modifierSource = player.module().compositeTransactionModifiers();
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
