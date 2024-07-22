package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import java.util.ArrayList;
import java.util.List;

@Model("zombies.upgrade.effect.composite")
@Cache
public class CompositeEffectComponent implements UpgradeEffectComponent {
    private final List<UpgradeEffectComponent> delegates;

    @FactoryMethod
    public CompositeEffectComponent(@Child("delegates") List<UpgradeEffectComponent> delegates) {
        this.delegates = delegates;
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        List<UpgradeEffect> effects = new ArrayList<>(delegates.size());
        for (UpgradeEffectComponent component : delegates) {
            effects.add(component.apply(injectionStore, zombiesPlayer));
        }

        return new Internal(effects);
    }

    private static final class Internal implements UpgradeEffect {
        private final List<UpgradeEffect> effects;
        private final List<UpgradeEffect> tickables;
        private final boolean needsTicking;

        private Internal(List<UpgradeEffect> effects) {
            this.effects = effects;

            ArrayList<UpgradeEffect> tickables = null;
            for (UpgradeEffect effect : effects) {
                if (effect.needsTicking()) {
                    (tickables = (tickables == null ? new ArrayList<>() : tickables)).add(effect);
                }
            }

            this.needsTicking = tickables != null;
            if (tickables != null) {
                tickables.trimToSize();
            }

            this.tickables = tickables;
        }

        @Override
        public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            for (UpgradeEffect effect : effects) {
                effect.apply(upgrade, zombiesPlayer, triggerData);
            }
        }

        @Override
        public void tick() {
            if (tickables == null) {
                return;
            }

            for (UpgradeEffect tickable : tickables) {
                tickable.tick();
            }
        }

        @Override
        public boolean needsTicking() {
            return needsTicking;
        }
    }
}
