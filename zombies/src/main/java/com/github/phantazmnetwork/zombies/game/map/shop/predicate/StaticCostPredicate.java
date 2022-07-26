package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.Prioritized;
import com.github.phantazmnetwork.commons.config.PrioritizedProcessor;
import com.github.phantazmnetwork.zombies.game.coin.PlayerCoins;
import com.github.phantazmnetwork.zombies.game.coin.Transaction;
import com.github.phantazmnetwork.zombies.game.coin.TransactionResult;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.steanky.element.core.annotation.ElementData;
import com.github.steanky.element.core.annotation.ElementModel;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.ProcessorMethod;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

@ElementModel("zombies.map.shop.predicate.static_cost")
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

    @FactoryMethod
    public StaticCostPredicate(@NotNull Data data) {
        super(data);
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return PROCESSOR;
    }

    @Override
    public boolean canHandleInteraction(@NotNull PlayerInteraction interaction) {
        Transaction transaction = new Transaction(data.cost);
        ZombiesPlayer player = interaction.getPlayer();
        PlayerCoins coins = player.getCoins();
        TransactionResult result = coins.runTransaction(transaction);
        if (coins.getCoins() + result.change() < 0) {
            player.getPlayerView().getPlayer().ifPresent(presentPlayer -> presentPlayer.sendMessage(data.message));
            return false;
        }

        return true;
    }

    @ElementData
    public record Data(int priority, int cost, @NotNull Component message) implements Keyed, Prioritized {
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "zombies.map.shop.predicate.static_cost");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }
}
