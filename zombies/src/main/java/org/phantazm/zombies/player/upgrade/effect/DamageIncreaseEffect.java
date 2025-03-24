package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.event.trait.SettableDamageAmountEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.effect.scaling.Scaling;
import org.phantazm.zombies.player.upgrade.effect.scaling.ScalingComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.effect.damage_increase")
@Cache
public class DamageIncreaseEffect implements UpgradeEffectComponent {
    private final ScalingComponent scalingComponent;

    @FactoryMethod
    public DamageIncreaseEffect(@NotNull @Child("scaling") ScalingComponent scalingComponent) {
        this.scalingComponent = scalingComponent;
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(scalingComponent.apply(injectionStore, zombiesPlayer));
    }

    private static final class Internal extends SingleEventEffect<SettableDamageAmountEvent> {
        private final Scaling scaling;

        private Internal(Scaling scaling) {
            super(SettableDamageAmountEvent.class);
            this.scaling = scaling;
        }

        @Override
        protected void applyEvent(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData, @NotNull SettableDamageAmountEvent event) {
            double scaling = this.scaling.getMultiplier(upgrade, zombiesPlayer, triggerData);
            event.setDamageAmount((float) (event.damageAmount() * scaling));
        }
    }
}
