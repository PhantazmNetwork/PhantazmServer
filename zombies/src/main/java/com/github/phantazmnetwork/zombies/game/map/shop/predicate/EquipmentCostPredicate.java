package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.upgrade.Upgradable;
import com.github.phantazmnetwork.zombies.game.coin.ModifierSource;
import com.github.phantazmnetwork.zombies.game.coin.PlayerCoins;
import com.github.phantazmnetwork.zombies.game.coin.Transaction;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.steanky.element.core.annotation.*;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Model("zombies.map.shop.predicate.equipment_cost")
public class EquipmentCostPredicate extends PredicateBase<EquipmentCostPredicate.Data> {
    private final ModifierSource modifierSource;

    @FactoryMethod
    public EquipmentCostPredicate(@NotNull Data data,
            @NotNull @Dependency("zombies.dependency.map_object.modifier_source") ModifierSource modifierSource) {
        super(data);
        this.modifierSource = Objects.requireNonNull(modifierSource, "modifierSource");
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        ZombiesPlayer player = interaction.player();
        PlayerCoins coins = interaction.player().getModule().getCoins();
        Optional<Equipment> equipmentOptional = player.getHeldEquipment();
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
