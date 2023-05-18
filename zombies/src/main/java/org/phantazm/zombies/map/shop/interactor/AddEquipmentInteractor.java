package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.core.equipment.EquipmentHandler;
import org.phantazm.core.equipment.Upgradable;
import org.phantazm.core.equipment.UpgradePath;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

@Model("zombies.map.shop.interactor.add_equipment")
public class AddEquipmentInteractor extends InteractorBase<AddEquipmentInteractor.Data> {
    private final UpgradePath upgradePath;

    @FactoryMethod
    public AddEquipmentInteractor(@NotNull Data data, @NotNull @Child("upgrade_path") UpgradePath upgradePath) {
        super(data);
        this.upgradePath = Objects.requireNonNull(upgradePath, "upgradePath");
    }

    @Override
    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        ZombiesPlayer player = interaction.player();
        for (Equipment equipment : player.module().getEquipmentHandler().getEquipment(data.groupKey)) {
            if (equipment.key().equals(data.equipmentKey) && equipment instanceof Upgradable upgradable) {
                upgradePath.nextUpgrade(upgradable.currentLevel()).ifPresent(upgradeKey -> {
                    if (upgradable.getSuggestedUpgrades().contains(upgradeKey)) {
                        upgradable.setLevel(upgradeKey);
                    }
                });

                return;
            }
        }

        addEquipment(player);
    }

    private void addEquipment(ZombiesPlayer player) {
        EquipmentHandler handler = player.module().getEquipmentHandler();
        if (handler.canAddEquipment(data.groupKey)) {
            player.module().getEquipmentCreator().createEquipment(data.equipmentKey)
                    .ifPresent(value -> handler.addEquipment(value, data.groupKey));
        }
    }

    @DataObject
    public record Data(@NotNull Key equipmentKey,
                       @NotNull Key groupKey,
                       @NotNull @ChildPath("upgrade_path") String upgradePath) {
    }
}
