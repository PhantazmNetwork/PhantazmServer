package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.component.KeyedConfigProcessor;
import com.github.phantazmnetwork.commons.component.annotation.ComponentData;
import com.github.phantazmnetwork.commons.component.annotation.ComponentFactory;
import com.github.phantazmnetwork.commons.component.annotation.ComponentModel;
import com.github.phantazmnetwork.commons.component.annotation.ComponentProcessor;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.equipment.Upgradable;
import com.github.phantazmnetwork.zombies.game.coin.Transaction;
import com.github.phantazmnetwork.zombies.game.coin.TransactionResult;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.map.shop.Shop;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

@ComponentModel("phantazm:zombies.map.shop.interactor.equipment")
public class UpgradeShopInteractor extends TransactionalInteractor {
    private static final KeyedConfigProcessor<Data> PROCESSOR = new KeyedConfigProcessor<>() {
        private static final ConfigProcessor<Object2IntMap<Key>> MAP_PROCESSOR =
                ConfigProcessor.mapProcessor(AdventureConfigProcessors.key(), ConfigProcessor.INTEGER,
                                             Object2IntLinkedOpenHashMap::new
                );

        @Override
        public @NotNull Data dataFromNode(@NotNull ConfigNode node) throws ConfigProcessException {
            Object2IntMap<Key> upgrades = MAP_PROCESSOR.dataFromElement(node.getElementOrThrow("costs"));
            return new Data(upgrades);
        }

        @Override
        public @NotNull ConfigNode nodeFromData(@NotNull Data data) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(1);
            node.put("costs", MAP_PROCESSOR.elementFromData(data.costMap));
            return node;
        }
    };

    private final Data data;

    @ComponentProcessor
    public static @NotNull KeyedConfigProcessor<Data> processor() {
        return PROCESSOR;
    }

    @ComponentFactory
    public UpgradeShopInteractor(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @Nullable Pair<Transaction, Consumer<TransactionResult>> getTransactionHook(@NotNull Shop shop,
                                                                                       @NotNull PlayerInteraction interaction) {
        Optional<Equipment> equipmentOptional = interaction.getPlayer().getHeldEquipment();
        if (equipmentOptional.isPresent()) {
            Equipment equipment = equipmentOptional.get();
            if (equipment instanceof Upgradable upgradable) {
                Set<Key> possibleUpgrades = upgradable.getSuggestedUpgrades();
                for (Key key : possibleUpgrades) {
                    if (data.costMap.containsKey(key)) {
                        return Pair.of(new Transaction(Collections.emptyList(), -data.costMap.getInt(key)),
                                       result -> upgradable.setLevel(key)
                        );
                    }
                }
            }
        }

        return null;
    }

    @ComponentData
    public record Data(@NotNull Object2IntMap<Key> costMap) implements Keyed {
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "zombies.map.shop.interactor.equipment");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }
}
