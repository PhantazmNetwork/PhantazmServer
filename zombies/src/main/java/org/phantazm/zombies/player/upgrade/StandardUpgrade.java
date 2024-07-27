package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.effect.UpgradeEffect;
import org.phantazm.zombies.player.upgrade.effect.UpgradeEffectComponent;
import org.phantazm.zombies.player.upgrade.trigger.UpgradeTrigger;
import org.phantazm.zombies.player.upgrade.trigger.UpgradeTriggerComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Model("zombies.upgrade.standard")
@Cache
public class StandardUpgrade implements PlayerUpgradeComponent {
    private final List<UpgradeTriggerComponent> triggerComponents;
    private final UpgradeEffectComponent effectComponent;


    @FactoryMethod
    public StandardUpgrade(@Child("triggers") List<UpgradeTriggerComponent> triggerComponents,
        @Child("effect") UpgradeEffectComponent effectComponent) {
        this.triggerComponents = triggerComponents;
        this.effectComponent = effectComponent;
    }

    @Override
    public @NotNull PlayerUpgrade apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        List<UpgradeTrigger> triggers = new ArrayList<>(triggerComponents.size());
        for (UpgradeTriggerComponent component : triggerComponents) {
            triggers.add(component.apply(injectionStore, zombiesPlayer));
        }

        return new Internal(zombiesPlayer, triggers, effectComponent.apply(injectionStore, zombiesPlayer));
    }

    private static final class Internal implements PlayerUpgrade {
        private final ZombiesPlayer zombiesPlayer;
        private final List<UpgradeTrigger> triggers;
        private final UpgradeEffect effect;
        private final boolean needsTicking;

        private final AtomicBoolean activated;

        private Internal(ZombiesPlayer zombiesPlayer, List<UpgradeTrigger> triggers, UpgradeEffect effect) {
            this.zombiesPlayer = zombiesPlayer;
            this.triggers = triggers;
            this.effect = effect;
            this.needsTicking = effect.needsTicking();
            this.activated = new AtomicBoolean();
        }

        @Override
        public void start() {
            if (activated.compareAndSet(false, true)) {
                for (UpgradeTrigger trigger : triggers) {
                    trigger.arm(this, effect);
                }
            }
        }

        @Override
        public void tick(long time) {
            effect.tick();
        }

        @Override
        public void end() {
            if (activated.compareAndSet(true, false)) {
                for (UpgradeTrigger trigger : triggers) {
                    trigger.disarm();
                }

                effect.clear(this, zombiesPlayer);
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

    @Default("""
        {
          triggers=[]
        }
        """)
    @DataObject
    public record Data() {

    }
}
