package org.phantazm.mob2.selector;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
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
    public DistanceLimitingSelector(@NotNull Data data, @NotNull @Child("origin_selector") SelectorComponent originSelector,
        @NotNull @Child("delegate") SelectorComponent delegate) {
        this.data = data;
        this.originSelector = originSelector;
        this.delegate = delegate;
    }

    @Override
    public @NotNull Selector apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Internal(data, originSelector.apply(mob, injectionStore), delegate.apply(mob, injectionStore));
    }

    public enum Behavior {
        CLOSEST,
        FARTHEST,
        RANDOM
    }

    @DataObject
    public record Data(@NotNull @ChildPath("origin_selector") String originSelector,
        @NotNull @ChildPath("delegate") String delegate,
        @NotNull Behavior behavior,
        int amount) {
    }

    private record Internal(Data data,
        Selector originSelector,
        Selector delegateSelector) implements Selector {

        @Override
        public @NotNull Target select() {
            if (data.amount <= 0) {
                return Target.NONE;
            }

            Target origin = originSelector.select();
            Optional<? extends Point> originOptional = origin.location();
            if (originOptional.isEmpty()) {
                return Target.NONE;
            }

            Target delegate = delegateSelector.select();
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

