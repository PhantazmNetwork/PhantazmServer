package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.Cooldown;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.timer.Timer;
import org.phantazm.zombies.player.upgrade.timer.TimerComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.effect.cooldown")
@Cache
public class CooldownEffect implements UpgradeEffectComponent {
    private final TimerComponent timer;
    private final UpgradeEffectComponent delegate;

    @FactoryMethod
    public CooldownEffect(@NotNull @Child("timer") TimerComponent timer, @Child("delegate") UpgradeEffectComponent delegate) {
        this.timer = timer;
        this.delegate = delegate;
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(timer.apply(injectionStore, zombiesPlayer), delegate.apply(injectionStore, zombiesPlayer));
    }

    private static final class Internal implements UpgradeEffect {
        private final Timer timer;
        private final UpgradeEffect effect;
        private final boolean tickDelegate;

        private final Cooldown cooldown;

        private Internal(Timer timer, UpgradeEffect effect) {
            this.timer = timer;
            this.effect = effect;
            this.tickDelegate = effect.needsTicking();

            this.cooldown = Cooldown.cooldown();
        }

        @Override
        public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            if (cooldown.takeCooldown(timer.getAsInt())) {
                effect.apply(upgrade, zombiesPlayer, triggerData);
            }
        }

        @Override
        public void clear(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer) {
            effect.clear(upgrade, zombiesPlayer);
            cooldown.reset();
        }

        @Override
        public boolean needsTicking() {
            return true;
        }

        @Override
        public void tick() {
            cooldown.step();

            if (tickDelegate) {
                effect.tick();
            }
        }
    }
}
