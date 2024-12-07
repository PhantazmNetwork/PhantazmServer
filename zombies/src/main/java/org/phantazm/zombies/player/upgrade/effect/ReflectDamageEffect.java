package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.Target;
import org.phantazm.zombies.event.player.ZombiesPlayerDamageEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.effect.reflect_damage")
@Cache
public class ReflectDamageEffect implements UpgradeEffectComponent{
    private final Data data;
    private final SelectorComponent selectorComponent;

    @FactoryMethod
    public ReflectDamageEffect(@NotNull Data data,
        @NotNull @Child("selector")SelectorComponent selectorComponent) {
        this.data = data;
        this.selectorComponent = selectorComponent;
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, selectorComponent.apply(injectionStore, zombiesPlayer));
    }

    private static final class Internal extends SingleEventEffect<ZombiesPlayerDamageEvent> {
        private final Data data;
        private final Selector selector;
        private volatile boolean isBroadcasting = false;

        private Internal(Data data, Selector selector) {
            super(ZombiesPlayerDamageEvent.class);
            this.data = data;
            this.selector = selector;
        }

        @Override
        protected void applyEvent(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer, @NotNull TriggerData triggerData, @NotNull ZombiesPlayerDamageEvent zombiesPlayerDamageEvent) {
            if (isBroadcasting) return;

            Damage damage = zombiesPlayerDamageEvent.damage();
            float amount = damage.getAmount();

            Player player = zombiesPlayerDamageEvent.getPlayer();
            Target target = selector.select(upgrade, zombiesPlayer, triggerData);
            target.forType(LivingEntity.class, livingEntity -> {
                isBroadcasting = true;
                livingEntity.damage(new Damage(DamageType.GENERIC, player, player, player.getPosition(), (float)(amount * data.percentageReflected)));
                isBroadcasting = false;
            });

            if (data.avoidReflectedDamage) {
                zombiesPlayerDamageEvent.cause().setDamage(new Damage(damage.getType(), damage.getSource(), damage.getAttacker(),
                    damage.getSourcePosition(), (float)(amount * (1 - data.percentageReflected))));
            }
        }
    }

    @Default("""
        {
          avoidReflectedDamage=true
        }
        """)
    @DataObject
    public record Data(double percentageReflected,
        boolean avoidReflectedDamage) {}
}
