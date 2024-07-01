package org.phantazm.zombies.map.shop.predicate;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.core.equipment.EquipmentHandler;
import org.phantazm.core.equipment.Upgradable;
import org.phantazm.core.equipment.UpgradePath;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.ZombiesPlayerModule;

import java.util.Objects;
import java.util.Optional;

@Model("zombies.map.shop.predicate.equipment_space")
@Cache(false)
public class EquipmentSpacePredicate extends PredicateBase<EquipmentSpacePredicate.Data> {
    private final UpgradePath upgradePath;
    private final EquipmentPredicate equipmentPredicate;

    @FactoryMethod
    public EquipmentSpacePredicate(@NotNull Data data, @NotNull @Child("upgradePath") UpgradePath upgradePath,
        @NotNull @Child("equipmentPredicate") EquipmentPredicate equipmentPredicate) {
        super(data);
        this.upgradePath = Objects.requireNonNull(upgradePath);
        this.equipmentPredicate = Objects.requireNonNull(equipmentPredicate);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction, @NotNull Shop shop) {
        ZombiesPlayer player = interaction.player();
        ZombiesPlayerModule module = player.module();
        EquipmentHandler handler = module.getEquipmentHandler();

        if (data.mustHoldItemToUpgrade) {
            Optional<Equipment> heldEquipment = player.getHeldEquipment();

            //only check to upgrade the equipment we're holding
            if (data.allowUpgrade && heldEquipment.isPresent()) {
                Equipment equipment = heldEquipment.get();
                boolean sameType = equipment.key().equals(data.equipmentKey);

                if (sameType && equipment instanceof Upgradable upgradable) {
                    Optional<Key> upgradeKey = upgradePath.nextUpgrade(upgradable.currentLevel())
                        .filter(key -> upgradable.getSuggestedUpgrades().contains(key));

                    if (upgradeKey.isPresent()) {
                        return equipmentPredicate.canUpgrade(interaction, upgradable, upgradeKey.get());
                    }
                }

                //we already found equipment of this type, couldn't upgrade, and duplicates aren't allowed, so return
                if (sameType && !data.allowDuplicate) {
                    return false;
                }
            }

            if (!data.allowDuplicate) {
                //if duplicates are not allowed: check for them
                for (Equipment equipment : handler.getEquipment(data.groupKey)) {
                    if (equipment.key().equals(data.equipmentKey)) {
                        return false;
                    }
                }
            }

            if (module.getEquipmentHandler().canAddEquipment(data.groupKey)) {
                return equipmentPredicate.canAdd(interaction, data.equipmentKey);
            }
        }

        //check if we can upgrade our equipment
        boolean foundSameType = false;
        for (Equipment equipment : handler.getEquipment(data.groupKey)) {
            if (equipment.key().equals(data.equipmentKey)) {
                foundSameType = true;

                if (!data.allowUpgrade) {
                    break;
                }
            }

            if (data.allowUpgrade && equipment instanceof Upgradable upgradable) {
                Optional<Key> upgradeKey = upgradePath.nextUpgrade(upgradable.currentLevel())
                    .filter(key -> upgradable.getSuggestedUpgrades().contains(key));

                if (upgradeKey.isPresent()) {
                    return equipmentPredicate.canUpgrade(interaction, upgradable, upgradeKey.get());
                }
            }
        }

        //if we found the same type of equipment, couldn't upgrade it, and duplicates aren't allowed, return false
        if (foundSameType && !data.allowDuplicate) {
            return false;
        }

        //check if there's room to add equipment to this group
        return module.getEquipmentHandler().canAddEquipment(data.groupKey) &&
            equipmentPredicate.canAdd(interaction, data.equipmentKey);
    }

    @DataObject
    public record Data(
        @NotNull Key equipmentKey,
        @NotNull Key groupKey,
        boolean allowUpgrade,
        boolean mustHoldItemToUpgrade,
        boolean allowDuplicate) {
    }
}
