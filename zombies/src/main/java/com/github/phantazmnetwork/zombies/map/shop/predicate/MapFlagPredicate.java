package com.github.phantazmnetwork.zombies.map.shop.predicate;

import com.github.phantazmnetwork.zombies.map.Flaggable;
import com.github.phantazmnetwork.zombies.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("zombies.map.shop.predicate.map_flag")
public class MapFlagPredicate extends PredicateBase<MapFlagPredicate.Data> {
    private final Flaggable flaggable;

    @FactoryMethod
    public MapFlagPredicate(@NotNull Data data,
            @NotNull @Dependency("zombies.dependency.map_object.flaggable") Flaggable flaggable) {
        super(data);
        this.flaggable = Objects.requireNonNull(flaggable, "flaggable");
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        return flaggable.hasFlag(data.flag) != data.requireAbsent;
    }

    @DataObject
    public record Data(@NotNull Key flag, boolean requireAbsent) {
    }
}
