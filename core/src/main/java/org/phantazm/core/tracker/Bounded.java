package org.phantazm.core.tracker;

import com.github.steanky.vector.Bounds3I;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface Bounded {
    @NotNull @Unmodifiable List<Bounds3I> bounds();

    @NotNull Point center();
}
