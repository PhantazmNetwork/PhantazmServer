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
        private static final ConfigProcessor<Component> COMPONENT_PROCESSOR = AdventureConfigProcessors.component();

        @Override
        public @NotNull Data finishData(@NotNull ConfigNode node, int priority) throws ConfigProcessException {
            int cost = node.getNumberOrThrow("cost").intValue();
            Component message = COMPONENT_PROCESSOR.dataFromElement(node.getElementOrThrow("message"));
            return new Data(priority, cost, message);
        }

        @Override
        public @NotNull ConfigNode finishNode(@NotNull Data data) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(1);
            node.putNumber("cost", data.cost);
            node.put("message", COMPONENT_PROCESSOR.elementFromData(data.message));
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
        ZombiesPlayer player = interaction.getPlayer();
        PlayerCoins coins = player.getCoins();
        TransactionResult result = coins.runTransaction(new Transaction(modifiers, -data.cost));

        if (!result.isAffordable(coins)) {
            player.getPlayerView().getPlayer().ifPresent(presentPlayer -> presentPlayer.sendMessage(data.message));
            return false;
        }

        return true;
    }

    @DataObject
    public record Data(int priority, int cost, @NotNull Component message) implements Prioritized {
    }
}
