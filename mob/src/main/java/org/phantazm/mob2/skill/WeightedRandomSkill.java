package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Trigger;

import java.util.*;

@Model("mob.skill.weighted_random")
@Cache
public class WeightedRandomSkill implements SkillComponent {
    private final Data data;
    private final Random random;
    private final List<SkillComponent> delegates;

    @FactoryMethod
    public WeightedRandomSkill(@NotNull Data data, @NotNull @Child("delegates") List<SkillComponent> delegates) {
        this.data = data;
        this.random = new Random();
        this.delegates = delegates;
    }

    @Override
    public @NotNull Skill apply(@NotNull ExtensionHolder holder) {
        List<Skill> delegates = new ArrayList<>(this.delegates.size());
        for (SkillComponent component : this.delegates) {
            delegates.add(component.apply(holder));
        }

        return new Internal(data, delegates, random);
    }

    @Default("""
        {
          trigger=null,
          weights=null
        }
        """)
    @DataObject
    public record Data(@Nullable Trigger trigger,
        int[] weights) {
    }

    private static final class Internal implements Skill {
        private final Data data;
        private final Random random;
        private final boolean needsTicking;

        private final List<Skill> tickingDelegates;
        private final List<DelegateEntry> delegates;

        private final int cap;

        private record DelegateEntry(Skill delegate,
            int weight) implements Comparable<DelegateEntry> {

            @Override
            public int compareTo(@NotNull WeightedRandomSkill.Internal.DelegateEntry o) {
                return Integer.compare(this.weight, o.weight);
            }
        }

        private Internal(Data data, List<Skill> delegates, Random random) {
            this.data = data;
            this.random = random;

            if (delegates.isEmpty()) {
                this.needsTicking = false;
                this.tickingDelegates = List.of();
                this.delegates = List.of();
                this.cap = 0;
                return;
            }

            List<Skill> tickingDelegates = new ArrayList<>(delegates.size());
            for (Skill delegate : delegates) {
                if (delegate.needsTicking()) {
                    tickingDelegates.add(delegate);
                }
            }

            this.needsTicking = true;
            this.tickingDelegates = List.copyOf(tickingDelegates);

            int[] weights = ensureLength(data.weights, delegates.size());
            removeZeroes(weights);

            List<DelegateEntry> delegateEntries = new ArrayList<>(delegates.size());
            for (int i = 0; i < weights.length; i++) {
                delegateEntries.add(new DelegateEntry(delegates.get(i), weights[i]));
            }

            Collections.sort(delegateEntries);

            this.delegates = List.copyOf(delegateEntries);

            int sum = 0;
            for (int weight : weights) {
                sum += weight;
            }

            this.cap = sum;
        }

        private static void removeZeroes(int[] input) {
            int smallestIndex = -1;
            int smallestValue = Integer.MAX_VALUE;
            for (int i = 0; i < input.length; i++) {
                int value = input[i];
                if (value <= 0 && value < smallestValue) {
                    smallestIndex = i;
                    smallestValue = value;
                }
            }

            if (smallestIndex == -1) {
                return;
            }

            int offset = 1 - smallestValue;
            for (int i = 0; i < input.length; i++) {
                input[i] += offset;
            }
        }

        private static int[] ensureLength(int[] input, int targetLen) {
            if (targetLen == 0) {
                return new int[0];
            }

            if (input == null) {
                return new int[targetLen];
            }

            if (input.length == targetLen) {
                return input;
            }

            return Arrays.copyOf(input, targetLen);
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }

        @Override
        public void init(@NotNull Mob mob) {
            for (DelegateEntry delegate : delegates) {
                delegate.delegate.init(mob);
            }
        }

        @Override
        public void use(@NotNull Mob mob) {
            if (delegates.isEmpty()) {
                return;
            }

            int choice = random.nextInt(cap);

            double sum = 0;
            for (DelegateEntry entry : delegates) {
                sum += entry.weight;

                if (choice < sum) {
                    entry.delegate.use(mob);
                    return;
                }
            }

            delegates.get(delegates.size() - 1).delegate.use(mob);
        }

        @Override
        public void tick(@NotNull Mob mob) {
            for (Skill delegate : tickingDelegates) {
                delegate.tick(mob);
            }
        }

        @Override
        public boolean needsTicking() {
            return needsTicking;
        }

        @Override
        public void end(@NotNull Mob mob) {
            for (DelegateEntry delegate : delegates) {
                delegate.delegate.end(mob);
            }
        }
    }
}
