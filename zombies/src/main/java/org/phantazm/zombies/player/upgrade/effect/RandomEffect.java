package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.effect.random")
@Cache
public class RandomEffect implements UpgradeEffectComponent {
    private final Data data;
    private final UpgradeEffectComponent delegate;

    @FactoryMethod
    public RandomEffect(@NotNull Data data, @Child("delegate") UpgradeEffectComponent delegate) {
        this.data = data;
        this.delegate = delegate;
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, delegate.apply(injectionStore, zombiesPlayer));
    }

    private static final class Internal implements UpgradeEffect {
        private final Data data;
        private final UpgradeEffect effect;
        private final boolean tickDelegate;


        private Internal(Data data, UpgradeEffect effect) {
            this.data = data;
            this.effect = effect;
            this.tickDelegate = effect.needsTicking();
        }

        @Override
        public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            if (Math.random() < data.chance) {
                effect.apply(upgrade, zombiesPlayer, triggerData);
            }
        }

        @Override
        public void clear(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer) {
            effect.clear(upgrade, zombiesPlayer);
        }

        @Override
        public boolean needsTicking() {
            return tickDelegate;
        }

        @Override
        public void tick() {
            effect.tick();
        }
    }

    @DataObject
    public record Data(double chance) {
    }
}
