package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.phantazmnetwork.zombies.game.map.Flaggable;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("zombies.map.shop.predicate.flag")
public class FlagPredicate extends PredicateBase<FlagPredicate.Data> {
    private final Flaggable flaggable;

    @FactoryMethod
    public FlagPredicate(@NotNull Data data,
            @NotNull @Dependency("zombies.dependency.map_object.flaggable") Flaggable flaggable) {
        super(data);
        this.flaggable = Objects.requireNonNull(flaggable, "flaggable");
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            private static final ConfigProcessor<Key> KEY_CONFIG_PROCESSOR = ConfigProcessors.key();

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Key flag = KEY_CONFIG_PROCESSOR.dataFromElement(element.getElementOrThrow("flag"));
                boolean requireAbsent = element.getBooleanOrThrow("requireAbsent");
                return new Data(flag, requireAbsent);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                return ConfigNode.of("flag", KEY_CONFIG_PROCESSOR.elementFromData(data.flag), "requireAbsent",
                        data.requireAbsent);
            }
        };
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        return flaggable.hasFlag(data.flag) != data.requireAbsent;
    }

    @DataObject
    public record Data(@NotNull Key flag, boolean requireAbsent) {
    }
}
