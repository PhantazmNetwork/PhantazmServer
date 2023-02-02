package org.phantazm.zombies.equipment.gun.target.entityfinder.positional;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

/**
 * A {@link PositionalEntityFinder} which finds all nearby {@link Entity}s.
 */
@Model("zombies.gun.entity_finder.positional.nearby")
@Cache
public class NearbyEntityFinder implements PositionalEntityFinder {

    private final Data data;

    /**
     * Creates a {@link NearbyEntityFinder}.
     *
     * @param data The {@link NearbyEntityFinder}'s {@link Data}
     */
    @FactoryMethod
    public NearbyEntityFinder(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull Collection<Entity> findEntities(@NotNull Instance instance, @NotNull Point start) {
        return instance.getNearbyEntities(start, data.range());
    }

    /**
     * Data for a {@link NearbyEntityFinder}.
     *
     * @param range The euclidean distance range to search for nearby {@link Entity}s
     */
    @DataObject
    public record Data(double range) {

    }

}
