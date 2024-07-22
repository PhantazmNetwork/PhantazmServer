package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.equipment.perk.effect.shot.ApplyFireShotEffect;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.effect.spread_enhanced_fire")
@Cache
public class SpreadEnhancedFireEffect implements UpgradeEffectComponent {
    private final Data data;
    private final SelectorComponent selectorComponent;

    @FactoryMethod
    public SpreadEnhancedFireEffect(@NotNull Data data,
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
                Instance instance = entity.getInstance();
                if (instance == null) {
                    return;
                }

                instance.getEntityTracker().nearbyEntities(entity.getPosition(), data.radius,
                    EntityTracker.Target.LIVING_ENTITIES, nearby -> {
                        if (nearby instanceof Mob mob) {
                            applyFire.perform(mob, zombiesPlayer, data.scale);
                        }
                    });
            });
        }
    }

    @DataObject
    public record Data(double radius,
        int cooldown,
        double scale) {

    }
}
