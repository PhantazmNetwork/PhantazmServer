package org.phantazm.zombies.map.shop.predicate;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.flag.Flaggable;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

import java.util.Objects;

@Model("zombies.map.shop.predicate.map_flag")
@Cache(false)
public class MapFlagPredicate extends PredicateBase<MapFlagPredicate.Data> {
    private final Flaggable flaggable;

    @FactoryMethod
    public MapFlagPredicate(@NotNull Data data, @NotNull Flaggable flaggable) {
        super(data);
        this.flaggable = Objects.requireNonNull(flaggable);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction, @NotNull Shop shop) {
        return flaggable.hasFlag(data.flag) != data.requireAbsent;
    }

    @DataObject
    public record Data(@NotNull Key flag,
        boolean requireAbsent) {
    }
}
