package com.github.phantazmnetwork.zombies.player.state.revive;

import com.github.phantazmnetwork.commons.Activable;
import com.github.phantazmnetwork.zombies.player.state.ZombiesPlayerState;
import com.github.phantazmnetwork.zombies.player.state.ZombiesPlayerStateKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class KnockedPlayerState implements ZombiesPlayerState {

    private final ReviveHandler reviveHandler;

    private final Collection<Activable> activables;

    public KnockedPlayerState(@NotNull ReviveHandler reviveHandler, @NotNull Collection<Activable> activables) {
        this.reviveHandler = Objects.requireNonNull(reviveHandler, "reviveHandler");
        this.activables = Objects.requireNonNull(activables, "activables");
    }

    public @NotNull ReviveHandler getReviveHandler() {
        return reviveHandler;
    }

    @Override
    public void start() {
        reviveHandler.start();
        for (Activable activable : activables) {
            activable.start();
        }
    }

    @Override
    public @NotNull Optional<ZombiesPlayerState> tick(long time) {
        reviveHandler.tick(time);
        for (Activable activable : activables) {
            activable.tick(time);
        }
        return reviveHandler.getSuggestedState();
    }

    @Override
    public void end() {
        reviveHandler.end();
        for (Activable activable : activables) {
            activable.end();
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.text("KNOCKED", NamedTextColor.YELLOW);
    }

    @Override
    public @NotNull Key key() {
        return ZombiesPlayerStateKeys.KNOCKED.key();
    }

}
