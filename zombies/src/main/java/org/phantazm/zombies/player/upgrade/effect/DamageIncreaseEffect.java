package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.event.trait.SettableDamageAmountEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.effect.damage_increase")
@Cache
public class DamageIncreaseEffect implements UpgradeEffectComponent {
    private final Data data;
    private final SelectorComponent selectorComponent;

    @FactoryMethod
    public DamageIncreaseEffect(@NotNull Data data,
        @NotNull @Child("selector") SelectorComponent selectorComponent) {
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

        private Internal(Data data, Selector selector) {
            super(Event.class);
            this.data = data;
            this.selector = selector;
        }

        @Override
        protected void applyEvent(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData, @NotNull Event event) {
            if (!(event instanceof SettableDamageAmountEvent settableDamageAmountEvent)) {
                return;
            }

            selector.select(upgrade, zombiesPlayer, triggerData).forType(Mob.class).ifPresent(mob -> {
                settableDamageAmountEvent.setDamageAmount((float) (settableDamageAmountEvent.damageAmount() *
                    data.multiplier));
            });
        }
    }

    @DataObject
    public record Data(double multiplier) {
    }
}
