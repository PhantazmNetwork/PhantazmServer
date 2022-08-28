package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.phantazmnetwork.zombies.game.map.Flaggable;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Model("zombies.map.shop.predicate.type")
public class TypePredicate extends PredicateBase<TypePredicate.Data> {
    @FactoryMethod
    public TypePredicate(@NotNull Data data) {
        super(data);
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            private static final ConfigProcessor<Set<Key>> KEY_CONFIG_PROCESSOR = ConfigProcessors.key().setProcessor();

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Set<Key> types = KEY_CONFIG_PROCESSOR.dataFromElement(element.getElementOrThrow("types"));
                boolean blacklist = element.getBooleanOrThrow("blacklist");
                return new Data(types, blacklist);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                return ConfigNode.of("types", KEY_CONFIG_PROCESSOR.elementFromData(data.types), "blacklist",
                        data.blacklist);
            }
        };
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        return data.types.contains(interaction.key()) != data.blacklist;
    }

    @DataObject
    public record Data(@NotNull Set<Key> types, boolean blacklist) {
    }
}
