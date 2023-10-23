package org.phantazm.core.equipment;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.tick.Activable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class LeveledEquipment<TEquipment extends Equipment & UpgradeNode> implements Equipment, Upgradable, Activable {
    private final Key equipmentKey;
    private final Map<Key, ? extends TEquipment> levelMap;

    private Key currentLevelKey;
    private TEquipment currentLevel;

    public LeveledEquipment(@NotNull Key equipmentKey, @NotNull Key rootLevel,
        @NotNull Map<Key, ? extends TEquipment> levelMap) {
        this.equipmentKey = Objects.requireNonNull(equipmentKey);
        this.levelMap = Map.copyOf(levelMap);

        TEquipment equipment = this.levelMap.get(rootLevel);
        if (equipment == null) {
            throw new IllegalArgumentException("Root equipment does not exist");
        }

        this.currentLevelKey = rootLevel;
        this.currentLevel = equipment;
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

    @Override
    public void setSelected(boolean selected) {
        currentLevel.setSelected(selected);
    }

    @Override
    public void rightClick() {
        currentLevel.rightClick();
    }

    @Override
    public void leftClick() {
        currentLevel.leftClick();
    }

    @Override
    public void attack(@NotNull Entity target) {
        currentLevel.attack(target);
    }

    @Override
    public @Unmodifiable
    @NotNull Set<Key> getSuggestedUpgrades() {
        return currentLevel.upgrades();
    }

    @Override
    public @Unmodifiable
    @NotNull Set<Key> getLevels() {
        return levelMap.keySet();
    }

    @Override
    public void setLevel(@NotNull Key key) {
        if (key.equals(currentLevelKey)) {
            return;
        }

        TEquipment newLevel = levelMap.get(key);
        if (newLevel == null) {
            throw new IllegalArgumentException("Level '" + key + "' does not exist");
        }

        this.currentLevel.end();
        this.currentLevelKey = key;
        this.currentLevel = newLevel;
        this.currentLevel.start();
    }

    @Override
    public @NotNull Key currentLevel() {
        return currentLevelKey;
    }

    @Override
    public @NotNull Key key() {
        return equipmentKey;
    }

    @Override
    public @NotNull ItemStack getItemStack() {
        return currentLevel.getItemStack();
    }

    @Override
    public boolean shouldRedraw() {
        return currentLevel.shouldRedraw();
    }
}
