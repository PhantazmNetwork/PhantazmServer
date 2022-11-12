package com.github.phantazmnetwork.zombies.map.shop.predicate;

import com.github.phantazmnetwork.zombies.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Model("zombies.map.shop.predicate.type")
public class TypePredicate extends PredicateBase<TypePredicate.Data> {
    @FactoryMethod
    public TypePredicate(@NotNull Data data) {
        super(data);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        return data.types.contains(interaction.key()) != data.blacklist;
    }

    @DataObject
    public record Data(@NotNull Set<Key> types, boolean blacklist) {
    }
}
