package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.equipment.EquipmentCreator;
import com.github.phantazmnetwork.zombies.equipment.EquipmentHandler;
import com.github.phantazmnetwork.zombies.equipment.Upgradable;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Model("zombies.map.shop.interactor.add_equipment")
public class AddEquipmentInteractor extends InteractorBase<AddEquipmentInteractor.Data> {
    private final EquipmentCreator equipmentCreator;

    @FactoryMethod
    public AddEquipmentInteractor(@NotNull Data data,
            @NotNull @Dependency("zombies.dependency.map_object.equipment_creator") EquipmentCreator equipmentCreator) {
        super(data);
        this.equipmentCreator = Objects.requireNonNull(equipmentCreator, "equipmentCreator");
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            private static final ConfigProcessor<Key> KEY_PROCESSOR = ConfigProcessors.key();
            private static final ConfigProcessor<Map<Key, Key>> KEY_KEY_PROCESSOR =
                    ConfigProcessor.mapProcessor(KEY_PROCESSOR, KEY_PROCESSOR, HashMap::new);

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Key equipmentKey = KEY_PROCESSOR.dataFromElement(element.getElementOrThrow("equipmentKey"));
                Key groupKey = KEY_PROCESSOR.dataFromElement(element.getElementOrThrow("groupKey"));
                Map<Key, Key> upgrades = KEY_KEY_PROCESSOR.dataFromElement(element.getElementOrThrow("upgrades"));
                return new Data(equipmentKey, groupKey, upgrades);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                return ConfigNode.of("equipmentKey", KEY_PROCESSOR.elementFromData(data.equipmentKey), "groupKey",
                        KEY_PROCESSOR.elementFromData(data.groupKey), "upgrades",
                        KEY_KEY_PROCESSOR.elementFromData(data.upgrades));
            }
        };
    }

    @Override
    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        ZombiesPlayer player = interaction.player();
        for (Equipment equipment : player.getEquipment()) {
            if (equipment.key().equals(data.equipmentKey) && equipment instanceof Upgradable upgradable) {
                Key targetUpgrade = data.upgrades.get(upgradable.currentLevel());
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
    public record Data(@NotNull Key equipmentKey, @NotNull Key groupKey, @NotNull Map<Key, Key> upgrades) {
    }
}
