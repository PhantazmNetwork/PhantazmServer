package org.phantazm.zombies.powerup;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PowerupData(@NotNull Key id,
                          @NotNull ConfigList visuals,
                          @NotNull ConfigList actions,
                          @NotNull ConfigNode deactivationPredicate,
                          @Nullable ConfigNode pickupPredicate) implements Keyed {
    @Override
    public @NotNull Key key() {
        return id;
    }

    @Default("pickupPredicate")
    public static @NotNull ConfigElement defaultPickupPredicate() {
        return ConfigPrimitive.NULL;
    }
}
