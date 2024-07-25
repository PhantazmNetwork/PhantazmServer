package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.event.trait.SettableDamageAmountEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.effect.damage_increase")
@Cache
public class DamageIncreaseEffect implements UpgradeEffectComponent {
    private final Data data;

    @FactoryMethod
    public DamageIncreaseEffect(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data);
    }

    private static final class Internal extends SingleEventEffect<SettableDamageAmountEvent> {
        private final Data data;

        private Internal(Data data) {
            super(SettableDamageAmountEvent.class);
            this.data = data;
        }

        @Override
        protected void applyEvent(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData, @NotNull SettableDamageAmountEvent event) {
            event.setDamageAmount((float) (event.damageAmount() * data.multiplier));
        }
    }

    @DataObject
    public record Data(double multiplier) {
    }
}
