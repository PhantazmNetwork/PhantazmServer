package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.Prioritized;
import com.github.phantazmnetwork.commons.config.PrioritizedProcessor;
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@ElementModel("zombies.map.shop.predicate.flag_predicate")
public class FlagPredicate extends PredicateBase<FlagPredicate.Data> {
    private static final ConfigProcessor<Data> PROCESSOR = new PrioritizedProcessor<>() {
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

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return PROCESSOR;
    }

    private final ZombiesMap map;

    @FactoryMethod
    public FlagPredicate(@NotNull Data data, @NotNull @ElementDependency("zombies.dependency.map") ZombiesMap map) {
        super(data);
        this.map = Objects.requireNonNull(map, "map");
    }

    @Override
    public boolean canHandleInteraction(@NotNull PlayerInteraction interaction) {
        boolean result = map.hasFlag(data.flag) != data.requireAbsent;
        if (!result) {
            interaction.getPlayer().getPlayerView().getPlayer()
                       .ifPresent(presentPlayer -> presentPlayer.sendMessage(data.message));
        }

        return result;
    }

    @ElementData
    public record Data(int priority, @NotNull Key flag, @NotNull Component message, boolean requireAbsent)
            implements Keyed, Prioritized {
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "zombies.map.shop.predicate.flag_predicate");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }
}
