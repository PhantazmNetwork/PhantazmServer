package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.effect.UpgradeEffect;
import org.phantazm.zombies.player.upgrade.effect.UpgradeEffectComponent;
import org.phantazm.zombies.player.upgrade.trigger.UpgradeTrigger;
import org.phantazm.zombies.player.upgrade.trigger.UpgradeTriggerComponent;

import java.util.concurrent.atomic.AtomicBoolean;

@Model("zombies.upgrade.standard")
public class StandardUpgrade implements PlayerUpgradeComponent {
    private final UpgradeTriggerComponent triggerComponent;
    private final UpgradeEffectComponent effectComponent;


    @FactoryMethod
    public StandardUpgrade(@Child("trigger") UpgradeTriggerComponent triggerComponent,
        @Child("effect") UpgradeEffectComponent effectComponent) {
        this.triggerComponent = triggerComponent;
        this.effectComponent = effectComponent;
    }

    @Override
    public @NotNull PlayerUpgrade apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(triggerComponent.apply(injectionStore, zombiesPlayer),
            effectComponent.apply(injectionStore, zombiesPlayer));
    }

    private static final class Internal implements PlayerUpgrade {
        private final UpgradeTrigger trigger;
        private final UpgradeEffect effect;
        private final boolean needsTicking;

        private final AtomicBoolean activated;

        private Internal(UpgradeTrigger trigger, UpgradeEffect effect) {
            this.trigger = trigger;
            this.effect = effect;
            this.needsTicking = effect.needsTicking();
            this.activated = new AtomicBoolean();
        }

        @Override
        public void start() {
            if (activated.compareAndSet(false, true)) {
                trigger.arm(this, effect);
            }
        }

        @Override
        public void tick(long time) {
            effect.tick();
        }

        @Override
        public void end() {
            if (activated.compareAndSet(true, false)) {
                trigger.disarm();
            }
        }

        @Override
        public boolean needsTicking() {
            return needsTicking;
        }

        @Override
        public boolean isActivated() {
            return activated.get();
        }
    }
}
