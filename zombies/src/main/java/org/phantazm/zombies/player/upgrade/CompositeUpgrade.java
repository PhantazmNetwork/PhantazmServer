package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.tick.Activable;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.ArrayList;
import java.util.List;

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

        return new Internal(player, upgradeList);
    }

    private static class Internal extends GuardedPlayerUpgrade {
        private final boolean needsTicking;

        private Internal(ZombiesPlayer zombiesPlayer, List<PlayerUpgrade> upgradeList) {
            super(Activable.threadsafeWrapper(new Activable() {
                private final List<PlayerUpgrade> tickables = tickables(upgradeList);

                @Override
                public void start() {
                    for (PlayerUpgrade upgrade : upgradeList) {
                        upgrade.start();
                    }
                }

                @Override
                public void tick(long time) {
                    for (PlayerUpgrade upgrade : tickables) {
                        upgrade.tick(time);
                    }
                }

                @Override
                public void end() {
                    for (PlayerUpgrade upgrade : upgradeList) {
                        upgrade.end();
                    }
                }

                private static List<PlayerUpgrade> tickables(List<PlayerUpgrade> upgrades) {
                    ArrayList<PlayerUpgrade> tickables = new ArrayList<>(Math.min(upgrades.size(), 5));
                    for (PlayerUpgrade playerUpgrade : upgrades) {
                        if (playerUpgrade.needsTicking()) {
                            tickables.add(playerUpgrade);
                        }
                    }

                    tickables.trimToSize();
                    return tickables.isEmpty() ? List.of() : tickables;
                }
            }), zombiesPlayer);

            boolean needsTicking = false;
            for (PlayerUpgrade upgrade : upgradeList) {
                if (upgrade.needsTicking()) {
                    needsTicking = true;
                    break;
                }
            }

            this.needsTicking = needsTicking;
        }

        @Override
        public boolean needsTicking() {
            return this.needsTicking;
        }
    }
}
