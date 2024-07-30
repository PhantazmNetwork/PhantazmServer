package org.phantazm.zombies.player.upgrade.trigger;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.mapper.annotation.Default;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.effect.UpgradeEffect;

import java.util.concurrent.atomic.AtomicBoolean;

@Model("zombies.upgrade.trigger.ignite")
@Cache
public class IgniteTrigger implements UpgradeTriggerComponent {
    private final Data data;

    @FactoryMethod
    public IgniteTrigger(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull UpgradeTrigger apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, zombiesPlayer);
    }

    private static final class Internal implements UpgradeTrigger {
        private final Data data;
        private final ZombiesPlayer zombiesPlayer;
        private final AtomicBoolean onFire;

        private volatile ArmData armData;

        private record ArmData(PlayerUpgrade upgrade,
            UpgradeEffect effect) {
        }

        private Internal(Data data, ZombiesPlayer zombiesPlayer) {
            this.data = data;
            this.zombiesPlayer = zombiesPlayer;
            this.onFire = new AtomicBoolean();
        }

        @Override
        public void arm(@NotNull PlayerUpgrade upgrade, @NotNull UpgradeEffect effect) {
            this.armData = new ArmData(upgrade, effect);
        }

        @Override
        public void disarm() {
            this.armData = null;
        }

        @Override
        public void tick(long time) {
            ArmData armData = this.armData;
            if (armData == null) {
                return;
            }

            zombiesPlayer.getPlayer().ifPresent(player -> {
                if (player.isOnFire()) {
                    if (this.onFire.compareAndSet(false, true)) {
                        armData.effect.apply(armData.upgrade, zombiesPlayer, TriggerData.EMPTY);
                    }
                } else if (data.clear && this.onFire.compareAndSet(true, false)) {
                    armData.effect.clear(armData.upgrade, zombiesPlayer);
                }
            });
        }

        @Override
        public boolean needsTicking() {
            return true;
        }
    }

    @Default("""
        {
          clear=true
        }
        """)
    @DataObject
    public record Data(boolean clear) {
    }
}
