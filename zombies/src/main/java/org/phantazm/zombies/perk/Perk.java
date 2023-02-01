package org.phantazm.zombies.perk;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.Activable;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.zombies.upgrade.Upgradable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Perk implements Activable, InventoryObject, Keyed, Upgradable {
    private final PerkInfo perkInfo;
    private final Map<Key, PerkLevel> perkLevels;
    private final Set<Key> unmodifiableLevels;

    private PerkLevel currentLevel;
    private boolean redrawFlag;

    public Perk(@NotNull PerkInfo perkInfo, @NotNull Map<Key, PerkLevel> perkLevels) {
        this.perkInfo = Objects.requireNonNull(perkInfo, "perkInfo");
        this.perkLevels = Objects.requireNonNull(perkLevels, "perkLevels");
        this.unmodifiableLevels = Set.copyOf(perkLevels.keySet());

        Key rootLevel = perkInfo.rootLevel();
        currentLevel = perkLevels.get(rootLevel);
        if (currentLevel == null) {
            throw new IllegalArgumentException(
                    "perk with root level key '" + rootLevel + "' does not have a corresponding perk level");
        }
    }

    @Override
    public @NotNull ItemStack getItemStack() {
        redrawFlag = false;
        return currentLevel.getItemStack();
    }

    @Override
    public boolean shouldRedraw() {
        return currentLevel.shouldRedraw() || redrawFlag;
    }

    @Override
    public @Unmodifiable @NotNull Set<Key> getSuggestedUpgrades() {
        return currentLevel.upgrades();
    }

    @Override
    public @Unmodifiable @NotNull Set<Key> getLevels() {
        return unmodifiableLevels;
    }

    @Override
    public void setLevel(@NotNull Key key) {
        PerkLevel newLevel = perkLevels.get(key);
        if (newLevel == null) {
            throw new IllegalArgumentException("no PerkLevel named '" + key + "'");
        }

        currentLevel.end();
        currentLevel = newLevel;

        newLevel.start();
        redrawFlag = true;
    }

    @Override
    public @NotNull Key currentLevel() {
        return currentLevel.key();
    }

    @Override
    public @NotNull Key key() {
        return perkInfo.perkKey();
    }

    @Override
    public void start() {
        currentLevel.start();
    }

    @Override
    public void tick(long time) {
        currentLevel.tick(time);
    }

    @Override
    public void end() {
        currentLevel.end();
    }
}
