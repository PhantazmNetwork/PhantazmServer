package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

@Model("zombies.map.shop.predicate.uuid")
public class UuidPredicate extends PredicateBase<UuidPredicate.Data> {
    @FactoryMethod
    public UuidPredicate(@NotNull Data data) {
        super(data);
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            private static final ConfigProcessor<Set<UUID>> UUID_SET_PROCESSOR = ConfigProcessors.uuid().setProcessor();

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement node) throws ConfigProcessException {
                Set<UUID> uuids = UUID_SET_PROCESSOR.dataFromElement(node.getElementOrThrow("uuids"));
                boolean blacklist = node.getBooleanOrThrow("blacklist");
                return new Data(uuids, blacklist);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                return ConfigNode.of("uuids", UUID_SET_PROCESSOR.elementFromData(data.uuids), "blacklist",
                        data.blacklist);
            }
        };
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        return data.blacklist != data.uuids.contains(interaction.player().getModule().getPlayerView().getUUID());
    }

    @DataObject
    public record Data(@NotNull Set<UUID> uuids, boolean blacklist) {
    }
}
