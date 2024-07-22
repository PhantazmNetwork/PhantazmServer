package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.Target;
import org.phantazm.zombies.event.trait.MobTargetEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.effect.heal_on_headshot")
@Cache
public class HealOnKillEffect implements UpgradeEffectComponent {
    private final Data data;
    private final SelectorComponent selectorComponent;

    @FactoryMethod
    public HealOnKillEffect(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selectorComponent) {
        this.data = data;
        this.selectorComponent = selectorComponent;
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, selectorComponent.apply(injectionStore, zombiesPlayer));
    }

    private static final class Internal extends SingleEventEffect<MobTargetEvent> {
        private final Data data;
        private final Selector selector;

        private Internal(Data data, Selector selector) {
            super(MobTargetEvent.class);
            this.data = data;
            this.selector = selector;
        }

        @Override
        protected void applyEvent(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData, @NotNull MobTargetEvent event) {
            double healAmount = event.target().hasDataTag(data.specialTag) ?
                data.specialHealAmount : data.healAmount;

            Target target = selector.select(upgrade, zombiesPlayer, triggerData);

            target.forType(LivingEntity.class, livingEntity -> {
                livingEntity.getAcquirable().sync(entity -> {
                    LivingEntity targetEntity = (LivingEntity) entity;
                    targetEntity.setHealth((float) (targetEntity.getHealth() + healAmount));
                });
            });
        }
    }

    @DataObject
    public record Data(double healAmount,
        double specialHealAmount,
        @NotNull Key specialTag) {
    }
}
