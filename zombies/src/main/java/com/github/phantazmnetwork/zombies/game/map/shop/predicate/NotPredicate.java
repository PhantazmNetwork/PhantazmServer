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

@Model("zombies.map.shop.predicate.not")
public class NotPredicate extends PredicateBase<NotPredicate.Data> {
    @ProcessorMethod
    public static ConfigProcessor<NotPredicate.Data> processor() {
        return new PrioritizedProcessor<>() {
            @Override
            public @NotNull Data finishData(@NotNull ConfigNode node, int priority) throws ConfigProcessException {
                String predicate = node.getStringOrThrow("predicate");
                return new Data(priority, predicate);
            }

            @Override
            public @NotNull ConfigNode finishNode(@NotNull Data data) {
                ConfigNode node = new LinkedConfigNode(2);
                node.putString("predicate", data.predicate);
                return node;
            }
        };
    }

    private final ShopPredicate predicate;

    @FactoryMethod
    public NotPredicate(@NotNull Data data, @DataName("predicate") ShopPredicate predicate) {
        super(data);
        this.predicate = Objects.requireNonNull(predicate, "predicate");
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        return !predicate.canInteract(interaction);
    }

    @DataObject
    public record Data(int priority, @DataPath("predicate") String predicate) implements Prioritized {
    }
}
