package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Model("zombies.upgrade.composite")
@Cache
public class CompositeUpgrade implements PlayerUpgradeComponent {
    private final List<PlayerUpgradeComponent> upgrades;

    @FactoryMethod
    public CompositeUpgrade(@NotNull @Child("upgrades") List<PlayerUpgradeComponent> upgrades) {
        this.upgrades = upgrades;
    }

    @Override
    public @NotNull PlayerUpgrade apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer player) {
        List<PlayerUpgrade> upgradeList = new ArrayList<>(upgrades.size());
        for (PlayerUpgradeComponent component : upgrades) {
            upgradeList.add(component.apply(injectionStore, player));
        }

        return new Internal(upgradeList);
    }

    private static class Internal implements PlayerUpgrade {
        private final List<PlayerUpgrade> upgrades;
        private final List<PlayerUpgrade> tickables;
        private final AtomicBoolean activated;

        private Internal(List<PlayerUpgrade> upgrades) {
            this.upgrades = upgrades;

            List<PlayerUpgrade> tickables = null;
            for (PlayerUpgrade upgrade : upgrades) {
                if (upgrade.needsTicking()) {
                    (tickables = (tickables == null ? new ArrayList<>(upgrades.size()) : tickables)).add(upgrade);
                }
            }

            this.tickables = tickables;
            this.activated = new AtomicBoolean();
        }

        @Override
        public void start() {
            if (activated.compareAndSet(false, true)) {
                for (PlayerUpgrade upgrade : upgrades) {
                    upgrade.start();
                }
            }
        }

        @Override
        public void tick(long time) {
            if (tickables == null) {
                return;
            }

            for (PlayerUpgrade tickable : tickables) {
                tickable.tick(time);
            }
        }

        @Override
        public void end() {
            if (activated.compareAndSet(true, false)) {
                for (PlayerUpgrade upgrade : upgrades) {
                    upgrade.end();
                }
            }
        }

        @Override
        public boolean needsTicking() {
            return tickables != null;
        }

        @Override
        public boolean isActivated() {
            return activated.get();
        }
    }
}
