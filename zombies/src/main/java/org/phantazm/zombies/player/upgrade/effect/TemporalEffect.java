package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.timer.Timer;
import org.phantazm.zombies.player.upgrade.timer.TimerComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import java.util.concurrent.atomic.AtomicInteger;

@Model("zombies.upgrade.effect.temporal")
@Cache
public class TemporalEffect implements UpgradeEffectComponent {
    private final TimerComponent timer;
    private final UpgradeEffectComponent delegate;

    @FactoryMethod
    public TemporalEffect(@NotNull @Child("timer") TimerComponent timer,
        @NotNull @Child("delegate") UpgradeEffectComponent delegate) {
        this.timer = timer;
        this.delegate = delegate;
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(timer.apply(injectionStore, zombiesPlayer), delegate.apply(injectionStore, zombiesPlayer));
    }

    private static final class Internal implements UpgradeEffect {
        private final Timer timer;
        private final UpgradeEffect delegate;

        private final AtomicInteger cooldown;
        private final boolean tickDelegate;

        private volatile ClearData clearData;

        private record ClearData(PlayerUpgrade upgrade,
            ZombiesPlayer zombiesPlayer) {
        }

        private Internal(Timer timer, UpgradeEffect delegate) {
            this.timer = timer;
            this.delegate = delegate;

            this.cooldown = new AtomicInteger();
            this.tickDelegate = delegate.needsTicking();
        }

        @Override
        public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            if (cooldown.compareAndSet(0, timer.getAsInt())) {
                this.clearData = new ClearData(upgrade, zombiesPlayer);
                delegate.apply(upgrade, zombiesPlayer, triggerData);
            }
        }

        @Override
        public void clear(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer) {
            if (cooldown.getAndSet(0) != 0) {
                this.clearData = null;
                delegate.clear(upgrade, zombiesPlayer);
            }
        }

        @Override
        public boolean needsTicking() {
            return true;
        }

        @Override
        public void tick() {
            if (cooldown.getAndUpdate(current -> Math.max(current - 1, 0)) == 1) {
                ClearData clearData = this.clearData;
                this.clearData = null;
                if (clearData != null) {
                    delegate.clear(clearData.upgrade, clearData.zombiesPlayer);
                }
            }

            if (tickDelegate) {
                delegate.tick();
            }
        }
    }
}
