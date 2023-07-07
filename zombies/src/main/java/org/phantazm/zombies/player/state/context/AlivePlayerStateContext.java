package org.phantazm.zombies.player.state.context;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record AlivePlayerStateContext(@Nullable Component reviverName,
                                      @Nullable Point reviveLocation, boolean isRevive) {

    public static @NotNull AlivePlayerStateContext regular() {
        return new AlivePlayerStateContext(null, null, false);
    }

    public static @NotNull AlivePlayerStateContext revive(@Nullable Component reviverName, @Nullable Point reviveLocation) {
        return new AlivePlayerStateContext(reviverName, reviveLocation, true);
    }

}
