package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;

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

        private Internal(List<UpgradeEffect> effects) {
            this.effects = effects;
        }

        @Override
        public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer) {
            for (UpgradeEffect effect : effects) {
                effect.apply(upgrade, zombiesPlayer);
            }
        }
    }
}
