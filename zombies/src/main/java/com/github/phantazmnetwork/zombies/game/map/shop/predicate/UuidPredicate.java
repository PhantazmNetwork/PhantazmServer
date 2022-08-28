package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.phantazmnetwork.commons.Prioritized;
import com.github.phantazmnetwork.commons.config.PrioritizedProcessor;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
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
        return new PrioritizedProcessor<>() {
            private static final ConfigProcessor<Set<UUID>> UUID_SET_PROCESSOR = ConfigProcessors.uuid().setProcessor();

            @Override
            public @NotNull Data finishData(@NotNull ConfigNode node, int priority) throws ConfigProcessException {
                Set<UUID> uuids = UUID_SET_PROCESSOR.dataFromElement(node.getElementOrThrow("uuids"));
                boolean blacklist = node.getBooleanOrThrow("blacklist");
                return new Data(priority, uuids, blacklist);
            }

            @Override
            public @NotNull ConfigNode finishNode(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(2);
                node.put("uuids", UUID_SET_PROCESSOR.elementFromData(data.uuidSet));
                node.putBoolean("blacklist", data.blacklist);
                return node;
            }
        };
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        return data.blacklist != data.uuidSet.contains(interaction.getPlayer().getPlayerView().getUUID());
    }

    @DataObject
    public record Data(int priority, @NotNull Set<UUID> uuidSet, boolean blacklist) implements Prioritized {
    }
}
