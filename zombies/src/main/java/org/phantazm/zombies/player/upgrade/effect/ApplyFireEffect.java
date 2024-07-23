package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.equipment.perk.effect.shot.ApplyFireShotEffect;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.effect.apply_fire")
@Cache
public class ApplyFireEffect implements UpgradeEffectComponent {
    private final Data data;
    private final SelectorComponent selectorComponent;

    @FactoryMethod
    public ApplyFireEffect(@NotNull Data data,
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
            ApplyFireShotEffect applyFire = zombiesPlayer.lookupShotEffect(ApplyFireShotEffect.class);
            if (applyFire == null) {
                return;
            }

            selector.select(upgrade, zombiesPlayer, triggerData).forType(Entity.class, entity -> {
                applyFire.perform(entity, zombiesPlayer, data.scale);
            });
        }
    }

    @DataObject
    public record Data(double scale) {

    }
}
