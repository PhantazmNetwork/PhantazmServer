package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.upgrade.Upgradable;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.map.shop.UpgradePath;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("zombies.map.shop.predicate.equipment_space")
public class EquipmentSpacePredicate extends PredicateBase<EquipmentSpacePredicate.Data> {
    private final UpgradePath upgradePath;

    @FactoryMethod
    public EquipmentSpacePredicate(@NotNull Data data, @NotNull @DataName("upgrade_path") UpgradePath upgradePath) {
        super(data);
        this.upgradePath = Objects.requireNonNull(upgradePath, "upgradePath");
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        ZombiesPlayer player = interaction.player();
        for (Equipment equipment : player.getModule().getEquipment()) {
            if (equipment.key().equals(data.equipmentKey) && equipment instanceof Upgradable upgradable) {
                return upgradable.getSuggestedUpgrades().contains(upgradePath.nextUpgrade(upgradable.currentLevel()));
            }
        }

        return interaction.player().getModule().getEquipmentHandler().canAddEquipment(data.groupKey);
    }

    @DataObject
    public record Data(@NotNull Key equipmentKey,
                       @NotNull Key groupKey,
                       @NotNull @DataPath("upgrade_path") String upgradePath) {
    }
}
