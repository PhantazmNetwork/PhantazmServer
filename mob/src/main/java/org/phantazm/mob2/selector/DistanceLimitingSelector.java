package org.phantazm.mob2.selector;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Model("mob.selector.distance_limiting")
@Cache
public class DistanceLimitingSelector implements SelectorComponent {
    private final Data data;
    private final SelectorComponent originSelector;
    private final SelectorComponent delegate;

    @FactoryMethod
    public DistanceLimitingSelector(@NotNull Data data, @NotNull @Child("originSelector") SelectorComponent originSelector,
        @NotNull @Child("delegate") SelectorComponent delegate) {
        this.data = data;
        this.originSelector = originSelector;
        this.delegate = delegate;
    }

    @Override
    public @NotNull Selector apply(@NotNull ExtensionHolder holder) {
        return new Internal(data, originSelector.apply(holder), delegate.apply(holder));
    }

    public enum Behavior {
        CLOSEST,
        FARTHEST,
        RANDOM
    }

    @DataObject
    public record Data(
        @NotNull Behavior behavior,
        int amount) {
    }

    private record Internal(Data data,
        Selector originSelector,
        Selector delegateSelector) implements Selector {

        @Override
        public @NotNull Target select(@NotNull Mob mob) {
            if (data.amount <= 0) {
                return Target.NONE;
            }

            Target origin = originSelector.select(mob);
            Optional<? extends Point> originOptional = origin.location();
            if (originOptional.isEmpty()) {
                return Target.NONE;
            }

            Target delegate = delegateSelector.select(mob);
            Collection<Target.TargetEntry> entries = delegate.entries();
            if (entries.isEmpty()) {
                return Target.NONE;
            }

            Point originPoint = originOptional.get();
            List<Target.TargetEntry> entriesCopy = new ArrayList<>(entries);
            int limit = Math.min(entries.size(), data.amount);
            switch (data.behavior) {
                case CLOSEST ->
                    entriesCopy.sort(Comparator.comparingDouble(o -> o.point().distanceSquared(originPoint)));
                case FARTHEST -> {
                    Comparator<Target.TargetEntry> comparator =
                        Comparator.comparingDouble(o -> o.point().distanceSquared(originPoint));

                    entriesCopy.sort(comparator.reversed());
                }
                case RANDOM -> Collections.shuffle(entriesCopy, ThreadLocalRandom.current());
            }

            return Target.entries(entriesCopy.subList(0, limit));
        }
    }
}

