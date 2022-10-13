package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

@Model("zombies.map.shop.predicate.uuid")
public class UuidPredicate extends PredicateBase<UuidPredicate.Data> {
    @FactoryMethod
    public UuidPredicate(@NotNull Data data) {
        super(data);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        return data.blacklist != data.uuids.contains(interaction.player().getModule().getPlayerView().getUUID());
    }

    @DataObject
    public record Data(@NotNull Set<UUID> uuids, boolean blacklist) {
    }
}
