package org.phantazm.mob2.selector;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.EntityTrackerUtils;
import org.phantazm.core.TrackerTargetType;
import org.phantazm.mob2.Mob;
import org.phantazm.core.Target;
import org.phantazm.mob2.validator.Validator;
import org.phantazm.mob2.validator.ValidatorComponent;

@Model("mob.selector.entities_in_area")
@Cache
public class EntitiesInAreaSelector implements SelectorComponent {
    private final Data data;
    private final SelectorComponent originSelector;
    private final ValidatorComponent validator;

    @FactoryMethod
    public EntitiesInAreaSelector(@NotNull Data data, @NotNull @Child("originSelector") SelectorComponent originSelector,
        @NotNull @Child("validator") ValidatorComponent validator) {
        this.data = data;
        this.originSelector = originSelector;
        this.validator = validator;
    }

    @Override
    public @NotNull Selector get() {
        return new Internal(originSelector.get(), validator.get(), data);
    }

    @Default("""
        {
          limitSelf=true,
          target='ENTITIES',
          range=-1.0,
          limit=-1
        }
        """)
    @DataObject
    public record Data(
        boolean limitSelf,
        @NotNull TrackerTargetType target,
        double range,
        int limit) {
    }

    private record Internal(Selector originSelector,
        Validator validator,
        Data data) implements Selector {
        @Override
        public @NotNull Target select(@NotNull Mob mob) {
            Instance instance = mob.getInstance();
            if (instance == null || data.limit == 0) {
                return Target.NONE;
            }

            return EntityTrackerUtils.select(instance, data.target.target(), originSelector.select(mob), data.limit,
                data.range, candidate -> {
                    return (!data.limitSelf || candidate != mob) && validator.valid(mob, candidate);
                });
        }
    }
}
