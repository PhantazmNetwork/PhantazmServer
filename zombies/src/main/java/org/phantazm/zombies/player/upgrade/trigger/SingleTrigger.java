package org.phantazm.zombies.player.upgrade.trigger;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.effect.UpgradeEffect;

@Model("zombies.upgrade.trigger.single")
@Cache
public class SingleTrigger implements UpgradeTriggerComponent {
    @FactoryMethod
    public SingleTrigger() {

    }

    @Override
    public @NotNull UpgradeTrigger apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(zombiesPlayer);
    }

    private static final class Internal implements UpgradeTrigger {
        private final ZombiesPlayer zombiesPlayer;

        private Internal(ZombiesPlayer zombiesPlayer) {
            this.zombiesPlayer = zombiesPlayer;
        }

        @Override
        public void arm(@NotNull PlayerUpgrade upgrade, @NotNull UpgradeEffect effect) {
            effect.apply(upgrade, zombiesPlayer, TriggerData.EMPTY);
        }

        @Override
        public void disarm() {
        }
    }
}
