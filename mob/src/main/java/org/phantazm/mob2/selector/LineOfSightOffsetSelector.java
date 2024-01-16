package org.phantazm.mob2.selector;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;

@Model("mob.selector.line_of_sight_offset")
@Cache
public class LineOfSightOffsetSelector implements SelectorComponent {
    private final Data data;

    @FactoryMethod
    public LineOfSightOffsetSelector(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull Selector apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Internal(mob, data);
    }

    @DataObject
    public record Data(double distance,
        boolean ignorePitch,
        boolean ignoreYaw) {
        @Default("ignorePitch")
        public static @NotNull ConfigElement defaultIgnorePitch() {
            return ConfigPrimitive.FALSE;
        }

        @Default("ignoreYaw")
        public static @NotNull ConfigElement defaultIgnoreYaw() {
            return ConfigPrimitive.FALSE;
        }
    }

    private record Internal(Mob self,
        Data data) implements Selector {
        @Override
        public @NotNull Target select() {
            Pos eyePos = self.getPosition().add(new Vec(0, self.getEyeHeight(), 0));
            Vec lookVector = eyePos.direction();

            Vec offsetVector = lookVector.apply((x, y, z) -> {
                if (data.ignorePitch) {
                    return new Vec(x, 0, z);
                }

                if (data.ignoreYaw) {
                    return new Vec(0, y, 0);
                }

                return new Vec(x, y, z);
            }).mul(data.distance);

            return Target.points(eyePos.add(offsetVector));
        }
    }
}
