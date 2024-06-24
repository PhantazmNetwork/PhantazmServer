package org.phantazm.zombies.player.upgrade;

import net.kyori.adventure.key.Key;
import net.minestom.server.Tickable;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.*;

public class PlayerUpgradeHandler implements Tickable {
    private final Map<Key, PlayerUpgrade> upgrades;
    private final List<PlayerUpgrade> tickables;

    public PlayerUpgradeHandler(@NotNull Map<Key, PlayerUpgradeComponent> upgradeComponents,
        @NotNull ZombiesPlayer zombiesPlayer) {
        this.upgrades = new HashMap<>(upgradeComponents.size());
        List<PlayerUpgrade> tickables = new ArrayList<>(upgradeComponents.size());

        for (Map.Entry<Key, PlayerUpgradeComponent> entry : upgradeComponents.entrySet()) {
            PlayerUpgrade upgrade = entry.getValue().apply(InjectionStore.of(), zombiesPlayer);
            this.upgrades.put(entry.getKey(), upgrade);
            if (upgrade.needsTicking()) {
                tickables.add(upgrade);
            }
        }

        this.tickables = List.copyOf(tickables);
    }

    public void activateUpgrade(@NotNull Key key) {
        PlayerUpgrade upgrade = upgrades.get(key);
        if (upgrade != null) {
            upgrade.start();
        }
    }

    public void deactivateUpgrade(@NotNull Key key) {
        PlayerUpgrade upgrade = upgrades.get(key);
        if (upgrade != null) {
            upgrade.end();
        }
    }

    @Override
    public void tick(long time) {
        for (PlayerUpgrade upgrade : tickables) {
            upgrade.tick(time);
        }
    }
}
