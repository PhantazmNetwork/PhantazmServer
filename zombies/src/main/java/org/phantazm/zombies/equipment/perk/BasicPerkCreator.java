package org.phantazm.zombies.equipment.perk;

import net.kyori.adventure.key.Key;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.zombies.equipment.perk.level.PerkLevel;
import org.phantazm.zombies.equipment.perk.level.PerkLevelCreator;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class BasicPerkCreator implements PerkCreator {
    private final Key equipmentKey;
    private final Key rootLevel;
    private final Map<Key, PerkLevelCreator> perkLevelCreators;

    public BasicPerkCreator(@NotNull Key equipmentKey, @NotNull Key rootLevel,
            @NotNull Map<Key, PerkLevelCreator> perkLevelCreators) {
        this.equipmentKey = Objects.requireNonNull(equipmentKey, "equipmentKey");
        this.rootLevel = Objects.requireNonNull(rootLevel, "rootLevel");
        this.perkLevelCreators = Map.copyOf(perkLevelCreators);

        if (!this.perkLevelCreators.containsKey(rootLevel)) {
            throw new IllegalArgumentException("Level map does not contain root level '" + rootLevel + "'");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull Perk forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        Map.Entry<Key, PerkLevel>[] newMapEntries = new Map.Entry[perkLevelCreators.size()];
        Iterator<Map.Entry<Key, PerkLevelCreator>> entryIterator = perkLevelCreators.entrySet().iterator();
        for (int i = 0; i < newMapEntries.length; i++) {
            Map.Entry<Key, PerkLevelCreator> entry = entryIterator.next();
            newMapEntries[i] = Map.entry(entry.getKey(), entry.getValue().forPlayer(zombiesPlayer));
        }

        return new Basic(equipmentKey, rootLevel, Map.ofEntries(newMapEntries));
    }

    private static class Basic implements Perk {
        private final Key equipmentKey;
        private final Map<Key, PerkLevel> perkLevelMap;

        private Key currentLevelKey;
        private PerkLevel currentLevel;

        private Basic(Key equipmentKey, Key rootLevel, Map<Key, PerkLevel> perkLevelMap) {
            this.equipmentKey = equipmentKey;
            this.perkLevelMap = Map.copyOf(perkLevelMap);

            this.currentLevelKey = Objects.requireNonNull(rootLevel);
            this.currentLevel = this.perkLevelMap.get(rootLevel);
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
        public @Unmodifiable @NotNull Set<Key> getSuggestedUpgrades() {
            return currentLevel.upgrades();
        }

        @Override
        public @Unmodifiable @NotNull Set<Key> getLevels() {
            return perkLevelMap.keySet();
        }

        @Override
        public void setLevel(@NotNull Key key) {
            if (key.equals(currentLevelKey)) {
                return;
            }

            PerkLevel newLevel = perkLevelMap.get(key);
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
}
