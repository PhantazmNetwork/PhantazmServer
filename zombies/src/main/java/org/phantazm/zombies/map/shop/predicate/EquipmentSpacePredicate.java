package org.phantazm.zombies.map.shop.predicate;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.Equipment;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.UpgradePath;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.upgrade.Upgradable;

import java.util.Objects;

@Model("zombies.map.shop.predicate.equipment_space")
public class EquipmentSpacePredicate extends PredicateBase<EquipmentSpacePredicate.Data> {
    private final UpgradePath upgradePath;

    @FactoryMethod
    public EquipmentSpacePredicate(@NotNull Data data, @NotNull @Child("upgrade_path") UpgradePath upgradePath) {
        super(data);
        this.upgradePath = Objects.requireNonNull(upgradePath, "upgradePath");
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        ZombiesPlayer player = interaction.player();
        for (Equipment equipment : player.getModule().getEquipment()) {
            if (equipment.key().equals(data.equipmentKey) && equipment instanceof Upgradable upgradable) {
                return upgradePath.nextUpgrade(upgradable.currentLevel())
                        .filter(key -> upgradable.getSuggestedUpgrades().contains(key)).isPresent();
            }
        }

        return interaction.player().getModule().getEquipmentHandler().canAddEquipment(data.groupKey);
    }

    @DataObject
    public record Data(@NotNull Key equipmentKey,
                       @NotNull Key groupKey,
                       @NotNull @ChildPath("upgrade_path") String upgradePath) {
    }
}
