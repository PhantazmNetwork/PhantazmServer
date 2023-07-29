package org.phantazm.server.config.player;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public record PlayerConfig(@NotNull String nameFormat, @NotNull Component joinMessage) {

    public static final PlayerConfig DEFAULT = new PlayerConfig("<green><name><green>", Component.empty());

}
