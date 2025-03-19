package org.phantazm.zombies.player.upgrade.selector;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.Target;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Model("zombies.upgrade.selector.random")
@Cache
public class RandomSelector implements SelectorComponent {
    private final Data data;
    private final SelectorComponent delegate;

    @FactoryMethod
    public RandomSelector(@NotNull Data data, @NotNull @Child("delegate") SelectorComponent delegate) {
        this.data = data;
        this.delegate = delegate;
    }

    @Override
    public @NotNull Selector apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, delegate.apply(injectionStore, zombiesPlayer));
    }

    private record Internal(Data data,
        Selector delegate) implements Selector {
        @Override
        public @NotNull Target select(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            if (data.count <= 0) {
                return Target.NONE;
            }

            Target target = delegate.select(upgrade, zombiesPlayer, triggerData);
            Collection<Target.TargetEntry> targetEntries = target.entries();
            if (data.count == targetEntries.size()) {
                return target;
            }

            List<Target.TargetEntry> oldEntries = new ArrayList<>(targetEntries);

            int count = Math.min(oldEntries.size(), data.count);
            List<Target.TargetEntry> newEntries = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                int idx = zombiesPlayer.getScene().map().objects().module().random().nextInt(oldEntries.size());
                newEntries.add(oldEntries.get(idx));
            }

            return Target.entries(newEntries);
        }
    }

    @DataObject
    @Default("""
        {
          count=1
        }
        """)
    public record Data(int count) {
    }
}
