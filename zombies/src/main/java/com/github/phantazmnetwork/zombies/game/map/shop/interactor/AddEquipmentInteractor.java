package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.equipment.EquipmentHandler;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.map.shop.UpgradePath;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.upgrade.Upgradable;
import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("zombies.map.shop.interactor.add_equipment")
public class AddEquipmentInteractor extends InteractorBase<AddEquipmentInteractor.Data> {
    private final UpgradePath upgradePath;

    @FactoryMethod
    public AddEquipmentInteractor(@NotNull Data data, @NotNull @DataName("upgrade_path") UpgradePath upgradePath) {
        super(data);
        this.upgradePath = Objects.requireNonNull(upgradePath, "upgradePath");
    }

    @Override
    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        ZombiesPlayer player = interaction.player();
        for (Equipment equipment : player.getModule().getEquipment()) {
            if (equipment.key().equals(data.equipmentKey) && equipment instanceof Upgradable upgradable) {
                Key targetUpgrade = upgradePath.nextUpgrade(upgradable.currentLevel());
                if (upgradable.getSuggestedUpgrades().contains(targetUpgrade)) {
                    upgradable.setLevel(targetUpgrade);
                }

                return;
            }
        }

        addEquipment(player);
    }

    private void addEquipment(ZombiesPlayer player) {
        EquipmentHandler handler = player.getModule().getEquipmentHandler();
        if (handler.canAddEquipment(data.groupKey)) {
            player.getModule().getEquipmentCreator().createEquipment(data.equipmentKey)
                    .ifPresent(value -> handler.addEquipment(value, data.groupKey));
        }
    }

    @DataObject
    public record Data(@NotNull Key equipmentKey,
                       @NotNull Key groupKey,
                       @NotNull @DataPath("upgrade_path") String upgradePath) {
    }
}
