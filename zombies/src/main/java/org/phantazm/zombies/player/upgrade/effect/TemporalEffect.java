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
import java.util.concurrent.atomic.AtomicReference;

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
        private final AtomicReference<ApplyData> applyData;

        private final boolean tickDelegate;

        private record ApplyData(PlayerUpgrade upgrade,
            ZombiesPlayer zombiesPlayer) {
        }

        private Internal(Timer timer, UpgradeEffect delegate) {
            this.timer = timer;
            this.delegate = delegate;

            this.cooldown = new AtomicInteger();
            this.applyData = new AtomicReference<>();

            this.tickDelegate = delegate.needsTicking();
        }

        @Override
        public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            if (cooldown.compareAndSet(0, timer.getAsInt())) {
                applyData.compareAndSet(null, new ApplyData(upgrade, zombiesPlayer));
            }
        }

        @Override
        public void clear(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer) {
            int oldTimer = cooldown.getAndSet(0);

            if (oldTimer != 0) {
                ApplyData oldData = applyData.getAndSet(null);
                if (oldData != null) {
                    delegate.clear(oldData.upgrade, oldData.zombiesPlayer);
                }
            }
        }

        @Override
        public boolean needsTicking() {
            return true;
        }

        @Override
        public void tick() {
            if (cooldown.getAndUpdate(current -> Math.max(current - 1, 0)) == 1) {
                ApplyData oldData = applyData.getAndSet(null);
                if (oldData != null) {
                    delegate.clear(oldData.upgrade, oldData.zombiesPlayer);
                }
            }

            if (tickDelegate) {
                delegate.tick();
            }
        }
    }
}
