package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.Prioritized;
import com.github.phantazmnetwork.commons.component.KeyedConfigProcessor;
import com.github.phantazmnetwork.commons.component.annotation.ComponentData;
import com.github.phantazmnetwork.commons.component.annotation.ComponentFactory;
import com.github.phantazmnetwork.commons.component.annotation.ComponentModel;
import com.github.phantazmnetwork.commons.component.annotation.ComponentProcessor;
import com.github.phantazmnetwork.commons.config.PrioritizedProcessor;
import com.github.phantazmnetwork.zombies.game.coin.PlayerCoins;
import com.github.phantazmnetwork.zombies.game.coin.Transaction;
import com.github.phantazmnetwork.zombies.game.coin.TransactionResult;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.map.shop.Shop;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

@ComponentModel("phantazm:zombies.map.shop.predicate.static_cost")
public class StaticCostPredicate extends PredicateBase<StaticCostPredicate.Data> {
    private static final PrioritizedProcessor<Data> PROCESSOR = new PrioritizedProcessor<>() {
        @Override
        public @NotNull Data finishData(@NotNull ConfigNode node, int priority) throws ConfigProcessException {
            int cost = node.getNumberOrThrow("cost").intValue();
            Component message =
                    AdventureConfigProcessors.component().dataFromElement(node.getElementOrThrow("message"));
            return new Data(priority, cost, message);
        }

        @Override
        public @NotNull ConfigNode finishNode(@NotNull Data data) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(1);
            node.putNumber("cost", data.cost);
            node.put("message", AdventureConfigProcessors.component().elementFromData(data.message));
            return node;
        }
    };

    @ComponentProcessor
    public static @NotNull KeyedConfigProcessor<Data> processor() {
        return PROCESSOR;
    }

    @ComponentFactory
    public StaticCostPredicate(@NotNull Data data) {
        super(data);
    }

    @Override
    public boolean canHandleInteraction(@NotNull Shop shop, @NotNull PlayerInteraction interaction) {
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

    @ComponentData
    public record Data(int priority, int cost, @NotNull Component message) implements Keyed, Prioritized {
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "zombies.map.shop.predicate.static_cost");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }
}
