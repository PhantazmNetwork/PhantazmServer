package org.phantazm.mob2.selector;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Model("mob.selector.line_of_sight_offset")
@Cache
public class LineOfSightOffsetSelector implements SelectorComponent {
    private final Data data;
    private final SelectorComponent delegate;

    @FactoryMethod
    public LineOfSightOffsetSelector(@NotNull Data data, @NotNull @Child("delegate") SelectorComponent delegate) {
        this.data = data;
        this.delegate = delegate;
    }

    @Override
    public @NotNull Selector apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Internal(data, delegate.apply(mob, injectionStore));
    }

    @DataObject
    public record Data(double distance,
        boolean ignorePitch,
        boolean ignoreYaw,
        @NotNull @ChildPath("delegate") String delegate) {
        @Default("ignorePitch")
        public static @NotNull ConfigElement defaultIgnorePitch() {
            return ConfigPrimitive.FALSE;
        }

        @Default("ignoreYaw")
        public static @NotNull ConfigElement defaultIgnoreYaw() {
            return ConfigPrimitive.FALSE;
        }
    }

    private record Internal(Data data,
        Selector delegate) implements Selector {
        @Override
        public @NotNull Target select() {
            Target delegateTarget = delegate.select();
            Collection<Target.TargetEntry> entries = delegateTarget.entries();
            if (entries.isEmpty()) {
                return Target.NONE;
            }

            List<Point> positions = new ArrayList<>(entries.size());
            for (Target.TargetEntry entry : entries) {
                Point startPos = entry.point();
                if (entry.isEntity()) {
                    startPos = startPos.add(0, entry.entityOptional().orElseThrow().getEyeHeight(), 0);
                }

                Vec lookVector = (startPos instanceof Pos pos) ? pos.direction() : Vec.ZERO;
                if (data.ignorePitch && data.ignoreYaw) {
                    lookVector = Vec.ZERO;
                } else if (data.ignoreYaw) {
                    lookVector = new Vec(0, lookVector.y(), 0);
                } else if (data.ignorePitch) {
                    lookVector = lookVector.withY(0);
                }

                positions.add(startPos.add(lookVector.mul(data.distance)));
            }

            return Target.points(positions);
        }
    }
}
