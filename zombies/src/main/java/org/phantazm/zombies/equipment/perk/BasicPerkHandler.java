package org.phantazm.zombies.equipment.perk;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Map;

public class BasicPerkHandler implements PerkHandler {
    private final Map<Key, PerkCreator> creatorMap;

    public BasicPerkHandler(@NotNull Map<Key, PerkCreator> creatorMap) {
        this.creatorMap = Map.copyOf(creatorMap);
    }

    @Override
    public @NotNull Perk forPlayer(@NotNull Key perkType, @NotNull ZombiesPlayer zombiesPlayer) {
        PerkCreator creator = creatorMap.get(perkType);
        if (creator == null) {
            throw new IllegalArgumentException("Perk type '" + perkType + "' does not exist");
        }

        return creator.forPlayer(zombiesPlayer);
    }

    @Override
    public boolean hasType(@NotNull Key perkType) {
        return creatorMap.containsKey(perkType);
    }
}
