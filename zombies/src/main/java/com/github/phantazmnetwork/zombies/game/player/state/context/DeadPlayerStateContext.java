package com.github.phantazmnetwork.zombies.game.player.state.context;

import com.github.phantazmnetwork.commons.Activable;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class DeadPlayerStateContext {

    private final Collection<Activable> activables;

    private final Component killer;

    private final Component deathRoomName;

    private final boolean isRejoin;

    protected DeadPlayerStateContext(@NotNull Collection<Activable> activables, @Nullable Component killer,
            @Nullable Component deathRoomName, boolean isRejoin) {
        this.activables = List.copyOf(activables);
        this.killer = killer;
        this.deathRoomName = deathRoomName;
        this.isRejoin = isRejoin;
    }

    public static DeadPlayerStateContext killed(@NotNull Collection<Activable> activables, @Nullable Component killer,
            @Nullable Component deathRoomName) {
        return new DeadPlayerStateContext(activables, killer, deathRoomName, false);
    }

    public static DeadPlayerStateContext rejoin(@NotNull Collection<Activable> activables) {
        return new DeadPlayerStateContext(activables, null, null, true);
    }

    public @NotNull @Unmodifiable Collection<Activable> getActivables() {
        return activables;
    }

    public @NotNull Optional<Component> getKiller() {
        return Optional.ofNullable(killer);
    }

    public @NotNull Optional<Component> getDeathRoomName() {
        return Optional.ofNullable(deathRoomName);
    }

    public boolean isRejoin() {
        return isRejoin;
    }
}
