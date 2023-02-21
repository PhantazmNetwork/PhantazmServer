package org.phantazm.zombies.equipment.perk;

import net.kyori.adventure.key.Key;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.zombies.upgrade.Upgradable;

import java.util.Objects;
import java.util.Set;

public class Perk implements Equipment, Upgradable {
    private final Key key;
    private final PerkModel model;

    private Key currentLevelKey;
    private PerkLevel currentLevel;

    public Perk(@NotNull Key key, @NotNull PerkModel model) {
        this.key = Objects.requireNonNull(key, "key");
        this.model = Objects.requireNonNull(model, "model");

        this.currentLevelKey = model.rootLevel();
        this.currentLevel = model.levels().get(currentLevelKey);
    }

    @Override
    public void tick(long time) {
        currentLevel.tick(time);
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
    public @NotNull Key key() {
        return key;
    }

    @Override
    public @Unmodifiable @NotNull Set<Key> getSuggestedUpgrades() {
        return null;
    }

    @Override
    public @Unmodifiable @NotNull Set<Key> getLevels() {
        return null;
    }

    @Override
    public void setLevel(@NotNull Key key) {

    }

    @Override
    public @NotNull Key currentLevel() {
        return currentLevelKey;
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
