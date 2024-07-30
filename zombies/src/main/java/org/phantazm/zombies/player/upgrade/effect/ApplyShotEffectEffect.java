package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.equipment.perk.effect.shot.ApplyFireShotEffect;
import org.phantazm.zombies.equipment.perk.effect.shot.ShotEffect;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import org.phantazm.zombies.equipment.perk.effect.shot.ApplyAttributeShotEffect;

import java.util.concurrent.atomic.AtomicBoolean;

@Model("zombies.upgrade.effect.apply_shot_effect")
@Cache
public class ApplyShotEffectEffect implements UpgradeEffectComponent {
    private final Data data;
    private final SelectorComponent selectorComponent;

    @FactoryMethod
    public ApplyShotEffectEffect(@NotNull Data data,
        @NotNull @Child("selector") SelectorComponent selectorComponent) {
        this.data = data;
        this.selectorComponent = selectorComponent;
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, selectorComponent.apply(injectionStore, zombiesPlayer), new AtomicBoolean());
    }

    private record Internal(Data data,
        Selector selector,
        AtomicBoolean active) implements UpgradeEffect {

        @Override
        public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            if (!active.compareAndSet(false, true)) {
                return;
            }

            try {
                ShotEffect shotEffect = data.shotEffectType.lookup(zombiesPlayer);
                if (shotEffect == null) {
                    return;
                }

                selector.select(upgrade, zombiesPlayer, triggerData).forType(Entity.class, entity ->
                    shotEffect.perform(entity, zombiesPlayer, data.scale));
            } finally {
                active.set(false);
            }
        }
    }

    public enum ShotEffectType {
        APPLY_FIRE(ApplyFireShotEffect.class),
        APPLY_ATTRIBUTE(ApplyAttributeShotEffect.class);

        private final Class<? extends ShotEffect> cls;

        ShotEffectType(Class<? extends ShotEffect> cls) {
            this.cls = cls;
        }

        public @Nullable ShotEffect lookup(@NotNull ZombiesPlayer zombiesPlayer) {
            return zombiesPlayer.lookupShotEffect(cls);
        }
    }

    @DataObject
    public record Data(double scale,
        @NotNull ShotEffectType shotEffectType) {

    }
}
