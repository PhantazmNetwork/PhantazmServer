package org.phantazm.zombies.map.handler;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.Tickable;

import java.util.List;

public class LuckyChestHandler implements Tickable {
    private final List<Key> chestEquipment;

    public LuckyChestHandler(@NotNull List<Key> chestEquipment, @NotNull Key jingleSong) {
        this.chestEquipment = List.copyOf(chestEquipment);
    }

    public @NotNull @Unmodifiable List<Key> chestEquipment() {
        return chestEquipment;
    }

    @Override
    public void tick(long time) {

    }
}
