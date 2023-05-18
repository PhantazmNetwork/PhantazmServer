package org.phantazm.core.tracker;

import com.github.steanky.vector.Bounds3I;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.VecUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class BoundedBase implements Bounded {
    protected final List<Bounds3I> bounds;
    protected final Point center;

    public BoundedBase(@NotNull Point shift, @NotNull Bounds3I... bounds) {
        Objects.requireNonNull(shift, "shift");
        Objects.requireNonNull(bounds, "bounds");

        if (bounds.length == 0) {
            this.bounds = List.of(Bounds3I.immutable(shift.blockX(), shift.blockY(), shift.blockZ(), 1, 1, 1));
            this.center = new Vec(shift.x(), shift.y(), shift.z()).add(0.5);
        }
        else {
            Bounds3I[] temp = new Bounds3I[bounds.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = bounds[i].immutable().shift(shift.blockX(), shift.blockY(), shift.blockZ());
            }

            this.bounds = List.of(temp);
            this.center = VecUtils.toPoint(Bounds3I.enclosingImmutable(temp).immutableCenter());
        }
    }

    public BoundedBase(@NotNull Point shift, @NotNull Collection<Bounds3I> bounds) {
        this(shift, bounds.toArray(Bounds3I[]::new));
    }

    @Override
    public @NotNull @Unmodifiable List<Bounds3I> bounds() {
        return bounds;
    }

    @Override
    public @NotNull Point center() {
        return center;
    }
}
