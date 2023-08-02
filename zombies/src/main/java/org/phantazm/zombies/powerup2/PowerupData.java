package org.phantazm.zombies.powerup2;

import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public record PowerupData(@NotNull Key id,
                          @NotNull ConfigList visuals,
                          @NotNull ConfigList actions,
                          @NotNull ConfigNode deactivationPredicate) implements Keyed {
    @Override
    public @NotNull Key key() {
        return id;
    }
}
