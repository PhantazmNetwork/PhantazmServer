package org.phantazm.zombies.player.state.context;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class KnockedPlayerStateContext {

    private final Point knockLocation;

    private final Component knockRoom;

    private final Component killer;

    private final Entity vehicle;

    public KnockedPlayerStateContext(@NotNull Point knockLocation, @Nullable Component knockRoom,
            @Nullable Component killer, @NotNull Entity vehicle) {
        this.knockLocation = Objects.requireNonNull(knockLocation, "knockLocation");
        this.knockRoom = knockRoom;
        this.killer = killer;
        this.vehicle = Objects.requireNonNull(vehicle, "vehicle");
    }

    public @NotNull Point getKnockLocation() {
        return knockLocation;
    }

    public Optional<Component> getKnockRoom() {
        return Optional.ofNullable(knockRoom);
    }

    public @NotNull Optional<Component> getKiller() {
        return Optional.ofNullable(killer);
    }

    public @NotNull Entity getVehicle() {
        return vehicle;
    }

}
