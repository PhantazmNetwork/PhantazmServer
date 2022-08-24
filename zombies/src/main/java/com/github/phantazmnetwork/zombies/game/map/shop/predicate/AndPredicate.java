package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import com.github.phantazmnetwork.commons.Prioritized;
import com.github.phantazmnetwork.commons.config.PrioritizedProcessor;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("zombies.map.shop.predicate.and")
public class AndPredicate extends PredicateBase<AndPredicate.Data> {
    @ProcessorMethod
    public static ConfigProcessor<AndPredicate.Data> processor() {
        return new PrioritizedProcessor<>() {
            @Override
            public @NotNull Data finishData(@NotNull ConfigNode node, int priority) throws ConfigProcessException {
                String first = node.getStringOrThrow("first");
                String second = node.getStringOrThrow("second");
                return new Data(priority, first, second);
            }

            @Override
            public @NotNull ConfigNode finishNode(@NotNull Data data) {
                ConfigNode node = new LinkedConfigNode(2);
                node.putString("first", data.first);
                node.putString("second", data.second);
                return node;
            }
        };
    }

    private final ShopPredicate first;
    private final ShopPredicate second;

    @FactoryMethod
    public AndPredicate(@NotNull Data data, @DataName("first") ShopPredicate first,
            @DataName("second") ShopPredicate second) {
        super(data);
        this.first = Objects.requireNonNull(first, "first");
        this.second = Objects.requireNonNull(second, "second");
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        return first.canInteract(interaction) && second.canInteract(interaction);
    }

    @DataObject
    public record Data(int priority, @DataPath("first") String first, @DataPath("second") String second)
            implements Prioritized {
    }
}
