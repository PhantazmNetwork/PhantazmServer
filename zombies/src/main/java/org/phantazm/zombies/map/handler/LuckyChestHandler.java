package org.phantazm.zombies.map.handler;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.Tickable;
import org.phantazm.zombies.map.luckychest.Jingle;

import java.util.List;
import java.util.Objects;

public class LuckyChestHandler implements Tickable {
    private final List<Key> chestEquipment;
    private final Jingle jingle;

    public LuckyChestHandler(@NotNull List<Key> chestEquipment, @NotNull Jingle jingle) {
        this.chestEquipment = List.copyOf(chestEquipment);
        this.jingle = Objects.requireNonNull(jingle, "jingle");
    }

    public @NotNull @Unmodifiable List<Key> chestEquipment() {
        return chestEquipment;
    }

    @Override
    public void tick(long time) {

    }
}
