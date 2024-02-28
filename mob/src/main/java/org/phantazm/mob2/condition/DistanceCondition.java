package org.phantazm.mob2.condition;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

@Model("mob.skill.condition.distance")
@Cache
public class DistanceCondition implements SkillConditionComponent {
    private final Data data;
    private final SelectorComponent originSelector;
    private final SelectorComponent targetSelector;

    @FactoryMethod
    public DistanceCondition(@NotNull Data data,
        @NotNull @Child("origin_selector") SelectorComponent originSelector,
        @NotNull @Child("target_selector") SelectorComponent targetSelector) {
        this.data = data;
        this.originSelector = originSelector;
        this.targetSelector = targetSelector;
    }

    @Override
    public @NotNull SkillCondition apply(@NotNull ExtensionHolder holder) {
        return new Internal(data, originSelector.apply(holder), targetSelector.apply(holder));
    }

    public enum Behavior {
        WITHIN,
        WITHOUT
    }

    public enum MultiPointHandling {
        ANY_MATCH,
        ALL_MATCH,
        EXACTLY_N_MATCH,
        LT_N_MATCH,
        GT_N_MATCH
    }

    @Default("""
        {
          type='WITHIN',
          multiPointHandling='ANY_MATCH',
          matchCount=-1
        }
        """)
    @DataObject
    public record Data(@NotNull @ChildPath("origin_selector") String originSelector,
        @NotNull @ChildPath("target_selector") String targetSelector,
        double distance,
        @NotNull DistanceCondition.Behavior behavior,
        @NotNull MultiPointHandling multiPointHandling,
        int matchCount) {

    }

    private record Internal(Data data,
        Selector originSelector,
        Selector targetSelector) implements SkillCondition {

        @Override
        public boolean test(@NotNull Mob mob) {
            Target origin = originSelector.select(mob);
            Target target = targetSelector.select(mob);

            int matchCount = 0;
            for (Point originPoint : origin.locations()) {
                for (Point targetPoint : target.locations()) {
                    double actualDistance = originPoint.distanceSquared(targetPoint);
                    double targetDistance = data.distance * data.distance;
                    if (switch (data.behavior) {
                        case WITHIN -> actualDistance < targetDistance;
                        case WITHOUT -> actualDistance > targetDistance;
                    }) {
                        if (data.multiPointHandling == MultiPointHandling.ANY_MATCH) {
                            return true;
                        }

                        matchCount++;
                    } else if (data.multiPointHandling == MultiPointHandling.ALL_MATCH) {
                        return false;
                    }
                }
            }

            return switch (data.multiPointHandling) {
                case ANY_MATCH -> false;
                case ALL_MATCH -> true;
                case EXACTLY_N_MATCH -> matchCount == data.matchCount;
                case LT_N_MATCH -> matchCount < data.matchCount;
                case GT_N_MATCH -> matchCount > data.matchCount;
            };
        }
    }
}
