package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Prioritized;
import com.github.phantazmnetwork.commons.config.PrioritizedProcessor;
import com.github.phantazmnetwork.zombies.game.coin.PlayerCoins;
import com.github.phantazmnetwork.zombies.game.coin.Transaction;
import com.github.phantazmnetwork.zombies.game.coin.TransactionResult;
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Model("zombies.map.shop.predicate.static_cost")
public class StaticCostPredicate extends PredicateBase<StaticCostPredicate.Data> {
    private static final ConfigProcessor<Data> PROCESSOR = new PrioritizedProcessor<>() {
        @Override
        public @NotNull Data finishData(@NotNull ConfigNode node, int priority) throws ConfigProcessException {
            int cost = node.getNumberOrThrow("cost").intValue();
            return new Data(priority, cost);
        }

        @Override
        public @NotNull ConfigNode finishNode(@NotNull Data data) {
            ConfigNode node = new LinkedConfigNode(1);
            node.putNumber("cost", data.cost);
            return node;
        }
    };

    private final Collection<Transaction.Modifier> modifiers;

    @FactoryMethod
    public StaticCostPredicate(@NotNull Data data, @NotNull @Dependency("zombies.dependency.shop.purchase_modifiers")
    Collection<Transaction.Modifier> modifiers) {
        super(data);
        this.modifiers = Objects.requireNonNull(modifiers, "modifiers");
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return PROCESSOR;
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        PlayerCoins coins = interaction.getPlayer().getCoins();
        return coins.runTransaction(new Transaction(modifiers, -data.cost)).isAffordable(coins);
    }

    @DataObject
    public record Data(int priority, int cost) implements Prioritized {
    }
}
