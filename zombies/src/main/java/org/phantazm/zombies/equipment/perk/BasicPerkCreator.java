package org.phantazm.zombies.equipment.perk;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.LeveledEquipment;
import org.phantazm.zombies.equipment.perk.level.PerkLevel;
import org.phantazm.zombies.equipment.perk.level.PerkLevelCreator;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class BasicPerkCreator implements PerkCreator {
    private final Key equipmentKey;
    private final Key rootLevel;
    private final Map<Key, PerkLevelCreator> perkLevelCreators;

    public BasicPerkCreator(@NotNull Key equipmentKey, @NotNull Key rootLevel,
        @NotNull Map<Key, PerkLevelCreator> perkLevelCreators) {
        this.equipmentKey = Objects.requireNonNull(equipmentKey);
        this.rootLevel = Objects.requireNonNull(rootLevel);
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

    private static class Basic extends LeveledEquipment<PerkLevel> implements Perk {
        private Basic(@NotNull Key equipmentKey, @NotNull Key rootLevel,
            @NotNull Map<Key, ? extends PerkLevel> levelMap) {
            super(equipmentKey, rootLevel, levelMap);
        }
    }
}
