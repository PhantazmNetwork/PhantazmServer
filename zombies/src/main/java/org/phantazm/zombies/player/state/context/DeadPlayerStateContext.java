package org.phantazm.zombies.player.state.context;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class DeadPlayerStateContext {

    private final Component killer;

    private final Component deathRoomName;

    private final boolean isRejoin;

    protected DeadPlayerStateContext(@Nullable Component killer, @Nullable Component deathRoomName, boolean isRejoin) {
        this.killer = killer;
        this.deathRoomName = deathRoomName;
        this.isRejoin = isRejoin;
    }

    public static DeadPlayerStateContext killed(@Nullable Component killer, @Nullable Component deathRoomName) {
        return new DeadPlayerStateContext(killer, deathRoomName, false);
    }

    public static DeadPlayerStateContext rejoin() {
        return new DeadPlayerStateContext(null, null, true);
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
