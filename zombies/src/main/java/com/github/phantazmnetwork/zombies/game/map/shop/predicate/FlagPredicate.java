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
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@ComponentModel("phantazm:zombies.map.shop.predicate.flag_predicate")
public class FlagPredicate extends PredicateBase<FlagPredicate.Data> {
    private static final KeyedConfigProcessor<Data> PROCESSOR = new PrioritizedProcessor<>() {
        @Override
        public @NotNull Data finishData(@NotNull ConfigNode node, int priority) throws ConfigProcessException {
            Key flag = AdventureConfigProcessors.key().dataFromElement(node.getElementOrThrow("flag"));
            Component message =
                    AdventureConfigProcessors.component().dataFromElement(node.getElementOrThrow("message"));
            boolean requireAbsent = node.getBooleanOrThrow("requireAbsent");
            return new Data(priority, flag, message, requireAbsent);
        }

        @Override
        public @NotNull ConfigNode finishNode(@NotNull Data data) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(3);
            node.put("flag", AdventureConfigProcessors.key().elementFromData(data.flag));
            node.put("message", AdventureConfigProcessors.component().elementFromData(data.message));
            node.putBoolean("requireAbsent", data.requireAbsent);
            return node;
        }
    };

    @ComponentProcessor
    public static @NotNull KeyedConfigProcessor<Data> processor() {
        return PROCESSOR;
    }

    private final ZombiesMap.Context context;

    @ComponentFactory
    public FlagPredicate(@NotNull Data data, ZombiesMap.@NotNull Context context) {
        super(data);
        this.context = Objects.requireNonNull(context, "context");
    }

    @Override
    public boolean canHandleInteraction(@NotNull PlayerInteraction interaction) {
        boolean result = context.map().hasFlag(data.flag) != data.requireAbsent;
        if (!result) {
            interaction.getPlayer().getPlayerView().getPlayer()
                       .ifPresent(presentPlayer -> presentPlayer.sendMessage(data.message));
        }

        return result;
    }

    @ComponentData
    public record Data(int priority, @NotNull Key flag, @NotNull Component message, boolean requireAbsent)
            implements Keyed, Prioritized {
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "zombies.map.shop.predicate.flag_predicate");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }
}
