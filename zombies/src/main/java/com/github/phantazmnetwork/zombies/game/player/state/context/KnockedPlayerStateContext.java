package com.github.phantazmnetwork.zombies.game.player.state.context;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class KnockedPlayerStateContext {

    private final Point knockLocation;

    private final Component killer;

    public KnockedPlayerStateContext(@NotNull Point knockLocation, @Nullable Component killer) {
        this.knockLocation = Objects.requireNonNull(knockLocation, "knockLocation");
        this.killer = killer;
    }

    public @NotNull Point getKnockLocation() {
        return knockLocation;
    }

    public @NotNull Optional<Component> getKiller() {
        return Optional.ofNullable(killer);
    }

}
