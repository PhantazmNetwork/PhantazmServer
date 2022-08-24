package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
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
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("zombies.map.shop.predicate.flag_predicate")
public class FlagPredicate extends PredicateBase<FlagPredicate.Data> {
    private final ZombiesMap map;

    @FactoryMethod
    public FlagPredicate(@NotNull Data data, @NotNull @Dependency("zombies.dependency.map") ZombiesMap map) {
        super(data);
        this.map = Objects.requireNonNull(map, "map");
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new PrioritizedProcessor<>() {
            @Override
            public @NotNull Data finishData(@NotNull ConfigNode node, int priority) throws ConfigProcessException {
                Key flag = AdventureConfigProcessors.key().dataFromElement(node.getElementOrThrow("flag"));
                Component message =
                        AdventureConfigProcessors.component().dataFromElement(node.getElementOrThrow("message"));
                boolean requireAbsent = node.getBooleanOrThrow("requireAbsent");
                return new Data(priority, flag, requireAbsent);
            }

            @Override
            public @NotNull ConfigNode finishNode(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(2);
                node.put("flag", AdventureConfigProcessors.key().elementFromData(data.flag));
                node.putBoolean("requireAbsent", data.requireAbsent);
                return node;
            }
        };
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        return map.hasFlag(data.flag) != data.requireAbsent;
    }

    @DataObject
    public record Data(int priority, @NotNull Key flag, boolean requireAbsent) implements Prioritized {
    }
}
