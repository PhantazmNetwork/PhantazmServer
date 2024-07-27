package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.event.trait.SettableDamageAmountEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.effect.damage_scaled_by_health")
@Cache
public class DamageScaledByHealthEffect implements UpgradeEffectComponent{
    private final Data data;
    private final SelectorComponent selectorComponent;

    @FactoryMethod
    public DamageScaledByHealthEffect(@NotNull Data data,
        @NotNull @Child("selector") SelectorComponent selectorComponent) {
        this.data = data;
        this.selectorComponent = selectorComponent;
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, selectorComponent.apply(injectionStore, zombiesPlayer));
    }

    private static final class Internal extends SingleEventEffect<SettableDamageAmountEvent> {
        private final Data data;
        private final Selector selector;

        private Internal(Data data, Selector selector) {
            super(SettableDamageAmountEvent.class);
            this.data = data;
            this.selector = selector;
        }

        @Override
        protected void applyEvent(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData, @NotNull SettableDamageAmountEvent event) {

            selector.select(upgrade, zombiesPlayer, triggerData).forType(LivingEntity.class).ifPresent(livingEntity -> {
                float currentDamage = event.damageAmount();
                float health;

                health = data.useTotalHealth ? livingEntity.getMaxHealth() : livingEntity.getHealth();

                float newDamage = health * (float)data.percentageOfHealth;

                if(data.preventDamageDecrease && newDamage < currentDamage) {
                    return;
                }

                event.setDamageAmount(newDamage);
            });
        }
    }


    @Default("""
        {
          useTotalHealth=false,
          preventDamageDecrease=true
        }
        """)
    @DataObject
    public record Data(
        double percentageOfHealth,
        boolean useTotalHealth,
        boolean preventDamageDecrease
    ) {}
}
