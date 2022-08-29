package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.equipment.EquipmentCreator;
import com.github.phantazmnetwork.zombies.equipment.EquipmentHandler;
import com.github.phantazmnetwork.zombies.equipment.upgrade.Upgradable;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.map.shop.UpgradePath;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("zombies.map.shop.interactor.add_equipment")
public class AddEquipmentInteractor extends InteractorBase<AddEquipmentInteractor.Data> {
    private final EquipmentCreator equipmentCreator;
    private final UpgradePath upgradePath;

    @FactoryMethod
    public AddEquipmentInteractor(@NotNull Data data,
            @NotNull @Dependency("zombies.dependency.map_object.equipment_creator") EquipmentCreator equipmentCreator,
            @NotNull @DataName("upgrade_path") UpgradePath upgradePath) {
        super(data);
        this.equipmentCreator = Objects.requireNonNull(equipmentCreator, "equipmentCreator");
        this.upgradePath = Objects.requireNonNull(upgradePath, "upgradePath");
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            private static final ConfigProcessor<Key> KEY_PROCESSOR = ConfigProcessors.key();

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Key equipmentKey = KEY_PROCESSOR.dataFromElement(element.getElementOrThrow("equipmentKey"));
                Key groupKey = KEY_PROCESSOR.dataFromElement(element.getElementOrThrow("groupKey"));
                String upgradePath = element.getStringOrThrow("upgradePath");
                return new Data(equipmentKey, groupKey, upgradePath);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                return ConfigNode.of("equipmentKey", KEY_PROCESSOR.elementFromData(data.equipmentKey), "groupKey",
                        KEY_PROCESSOR.elementFromData(data.groupKey), "upgradePath", data.upgradePath);
            }
        };
    }

    @Override
    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        ZombiesPlayer player = interaction.player();
        for (Equipment equipment : player.getEquipment()) {
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
        EquipmentHandler handler = player.getEquipmentHandler();
        if (handler.canAddEquipment(data.groupKey)) {
            equipmentCreator.createEquipment(data.equipmentKey)
                    .ifPresent(value -> handler.addEquipment(value, data.groupKey));
        }
    }

    @DataObject
    public record Data(@NotNull Key equipmentKey,
                       @NotNull Key groupKey,
                       @NotNull @DataPath("upgrade_path") String upgradePath) {
    }
}
