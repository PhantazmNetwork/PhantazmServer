package org.phantazm.zombies.powerup;

import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

@Default("""
    {
      pickupPredicate={},
      powerupEffect={}
    }
    """)
public record PowerupData(
    @NotNull Key id,
    @NotNull ConfigList visuals,
    @NotNull ConfigList actions,
    @NotNull ConfigNode deactivationPredicate,
    @NotNull ConfigNode pickupPredicate,
    @NotNull ConfigNode powerupEffect) implements Keyed {

    @Override
    public @NotNull Key key() {
        return id;
    }
}
