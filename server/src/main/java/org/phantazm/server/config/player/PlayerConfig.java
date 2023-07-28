package org.phantazm.server.config.player;

import org.jetbrains.annotations.NotNull;

public record PlayerConfig(@NotNull String nameFormat) {

    public static final PlayerConfig DEFAULT = new PlayerConfig("<green><name><green>");

}
