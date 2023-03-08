package org.phantazm.zombies.map;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.VecUtils;
import org.phantazm.core.tracker.BoundedBase;
import org.phantazm.zombies.map.shop.PlayerInteraction;

import java.util.List;
import java.util.Objects;

public class LuckyChest extends BoundedBase {
    private final List<Key> chestEquipment;
    private final Key song;

    private boolean enabled;

    public LuckyChest(@NotNull Point mapOrigin, @NotNull LuckyChestInfo info, @NotNull List<Key> chestEquipment,
            @NotNull Key song) {
        super(mapOrigin.add(VecUtils.toPoint(info.location())));
        this.chestEquipment = List.copyOf(chestEquipment);
        this.song = Objects.requireNonNull(song, "song");
    }

    public void handleInteraction(@NotNull PlayerInteraction interaction) {

    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
