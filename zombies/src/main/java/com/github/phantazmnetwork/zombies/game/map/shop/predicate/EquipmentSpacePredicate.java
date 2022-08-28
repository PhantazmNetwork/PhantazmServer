package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
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

@Model("zombies.map.shop.predicate.equipment_space")
public class EquipmentSpacePredicate extends PredicateBase<EquipmentSpacePredicate.Data> {
    @FactoryMethod
    public EquipmentSpacePredicate(@NotNull Data data) {
        super(data);
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            private static final ConfigProcessor<Key> KEY_PROCESSOR = ConfigProcessors.key();

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Key equipmentKey = KEY_PROCESSOR.dataFromElement(element.getElementOrThrow("equipmentKey"));
                Key groupKey = KEY_PROCESSOR.dataFromElement(element.getElementOrThrow("groupKey"));
                return new Data(equipmentKey, groupKey);
            }

            @Override
            public @NotNull ConfigNode elementFromData(@NotNull Data data) throws ConfigProcessException {
                return ConfigNode.of("equipmentKey", KEY_PROCESSOR.elementFromData(data.equipmentKey),
                        KEY_PROCESSOR.elementFromData(data.groupKey));
            }
        };
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        ZombiesPlayer player = interaction.player();
        for (Equipment equipment : player.getEquipment()) {
            if (equipment.key().equals(data.equipmentKey) && equipment instanceof Upgradable upgradable) {
                return !upgradable.getSuggestedUpgrades().isEmpty();
            }
        }

        return interaction.player().getEquipmentHandler().canAddEquipment(data.groupKey);
    }

    @DataObject
    public record Data(@NotNull Key equipmentKey, @NotNull Key groupKey) {
    }
}
