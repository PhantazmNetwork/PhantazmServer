package org.phantazm.zombies.player.upgrade;

import net.kyori.adventure.key.Key;
import net.minestom.server.Tickable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.event.player.ZombiesPlayerModifyUpgrade;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class PlayerUpgradeHandler implements Tickable {
    private final Map<Key, PlayerUpgradeComponent> upgradeComponents;
    private final Map<Key, PlayerUpgrade> upgrades;
    private final List<PlayerUpgrade> tickables;

    private final ZombiesPlayer zombiesPlayer;

    public PlayerUpgradeHandler(@NotNull Map<Key, PlayerUpgradeComponent> upgradeComponents,
        @NotNull ZombiesPlayer zombiesPlayer) {
        this.upgradeComponents = Map.copyOf(upgradeComponents);
        this.upgrades = new ConcurrentHashMap<>();
        this.tickables = new CopyOnWriteArrayList<>();

        this.zombiesPlayer = zombiesPlayer;
    }

    public @Unmodifiable Set<Key> validUpgrades() {
        return upgradeComponents.keySet();
    }

    private PlayerUpgrade createNewUpgrade(Key key) {
        PlayerUpgrade newUpgrade = upgradeComponents.get(key).apply(InjectionStore.of(), zombiesPlayer);
        if (newUpgrade.needsTicking()) {
            tickables.add(newUpgrade);
        }

        return newUpgrade;
    }

    public boolean isValidUpgrade(@NotNull Key key) {
        return upgradeComponents.containsKey(key);
    }

    public boolean activateUpgrade(@NotNull Key key) {
        if (!upgradeComponents.containsKey(key)) {
            return false;
        }

        if (zombiesPlayer.addActivable(upgrades.computeIfAbsent(key, this::createNewUpgrade))) {
            broadcastModifyUpgrade(key, true);
            return true;
        }

        return false;
    }

    public void deactivateUpgrade(@NotNull Key key) {
        if (!upgradeComponents.containsKey(key)) {
            return;
        }

        PlayerUpgrade upgrade = upgrades.get(key);
        if (upgrade == null) {
            return;
        }

        if (upgrade.needsTicking()) {
            tickables.remove(upgrade);
        }

        if (zombiesPlayer.removeActivable(upgrade)) {
            broadcastModifyUpgrade(key, false);
        }
    }

    public void broadcastModifyUpgrade(Key upgradeKey, boolean added) {
        zombiesPlayer.getPlayer().ifPresent(value -> zombiesPlayer.getScene().broadcastEvent(new ZombiesPlayerModifyUpgrade(value, zombiesPlayer,
            upgradeKey, added)));
    }

    public PlayerUpgrade getUpgrade(@NotNull Key key) {
        return upgrades.get(key);
    }

    public @NotNull Collection<Map.Entry<Key, PlayerUpgrade>> activeUpgrades() {
        return upgrades.entrySet().stream().filter(entry -> entry.getValue().isActivated()).collect(Collectors.toList());
    }

    public @NotNull ZombiesPlayer zombiesPlayer() {
        return zombiesPlayer;
    }

    @Override
    public void tick(long time) {
        for (PlayerUpgrade upgrade : tickables) {
            upgrade.tick(time);
        }
    }
}
