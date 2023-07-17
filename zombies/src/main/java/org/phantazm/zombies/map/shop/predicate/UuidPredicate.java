package org.phantazm.zombies.map.shop.predicate;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

import java.util.Set;
import java.util.UUID;

@Model("zombies.map.shop.predicate.uuid")
public class UuidPredicate extends PredicateBase<UuidPredicate.Data> {
    @FactoryMethod
    public UuidPredicate(@NotNull Data data) {
        super(data);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction, @NotNull Shop shop) {
        return data.blacklist != data.uuids.contains(interaction.player().getUUID());
    }

    @DataObject
    public record Data(@NotNull Set<UUID> uuids, boolean blacklist) {
    }
}
