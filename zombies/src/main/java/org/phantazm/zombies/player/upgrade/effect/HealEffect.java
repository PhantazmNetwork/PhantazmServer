package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.Event;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.event.trait.DamageEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.effect.heal")
@Cache
public class HealEffect implements UpgradeEffectComponent {
    private final Data data;
    private final SelectorComponent selectorComponent;

    @FactoryMethod
    public HealEffect(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selectorComponent) {
        this.data = data;
        this.selectorComponent = selectorComponent;
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, selectorComponent.apply(injectionStore, zombiesPlayer));
    }

    private static final class Internal extends SingleEventEffect<Event> {
        private final Data data;
        private final Selector selector;
        private final Tag<Boolean> tag;

        private Internal(Data data, Selector selector) {
            super(Event.class);
            this.data = data;
            this.selector = selector;
            this.tag = Tag.Boolean(data.tag);
        }

        @Override
        protected void applyEvent(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData, @NotNull Event event) {

            double healAmount;
            if (event instanceof DamageEvent damageEvent) {
                healAmount = damageEvent.damage().getTag(tag) ? data.specialHealAmount : data.healAmount;
            } else {
                healAmount = data.healAmount;
            }

            selector.select(upgrade, zombiesPlayer, triggerData).forType(LivingEntity.class, livingEntity -> {
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
        @NotNull String tag) {
    }
}
