package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Prioritized;
import com.github.phantazmnetwork.commons.config.PrioritizedProcessor;
import com.github.phantazmnetwork.zombies.game.coin.ModifierSource;
import com.github.phantazmnetwork.zombies.game.coin.PlayerCoins;
import com.github.phantazmnetwork.zombies.game.coin.Transaction;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("zombies.map.shop.predicate.static_cost")
public class StaticCostPredicate extends PredicateBase<StaticCostPredicate.Data> {
    private final ModifierSource modifierSource;

    @FactoryMethod
    public StaticCostPredicate(@NotNull Data data,
            @NotNull @Dependency("zombies.dependency.modifier_source") ModifierSource modifierSource) {
        super(data);
        this.modifierSource = Objects.requireNonNull(modifierSource, "modifierSource");
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new PrioritizedProcessor<>() {
            private static final ConfigProcessor<Key> KEY_PROCESSOR = AdventureConfigProcessors.key();

            @Override
            public @NotNull Data finishData(@NotNull ConfigNode node, int priority) throws ConfigProcessException {
                int cost = node.getNumberOrThrow("cost").intValue();
                Key modifier = KEY_PROCESSOR.dataFromElement(node.getElementOrThrow("modifierType"));
                return new Data(priority, cost, modifier);
            }

            @Override
            public @NotNull ConfigNode finishNode(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(2);
                node.putNumber("cost", data.cost);
                node.put("modifierType", KEY_PROCESSOR.elementFromData(data.modifierType));
                return node;
            }
        };
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        PlayerCoins coins = interaction.getPlayer().getCoins();
        Transaction transaction;
        if (!modifierSource.hasType(data.modifierType)) {
            transaction = new Transaction(-data.cost);
        }
        else {
            transaction = new Transaction(modifierSource.modifiers(data.modifierType), -data.cost);
        }

        return coins.runTransaction(transaction).isAffordable(coins);
    }

    @DataObject
    public record Data(int priority, int cost, @NotNull Key modifierType) implements Prioritized {
    }
}
