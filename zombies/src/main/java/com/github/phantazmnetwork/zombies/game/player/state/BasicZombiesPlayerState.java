package com.github.phantazmnetwork.zombies.game.player.state;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BasicZombiesPlayerState implements ZombiesPlayerState {

    private final Component displayName;

    private final Key key;

    private final Collection<Action> actions;

    public BasicZombiesPlayerState(@NotNull Component displayName, @NotNull Key key,
            @NotNull Collection<Action> actions) {
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.key = Objects.requireNonNull(key, "key");
        this.actions = List.copyOf(actions);
    }

    @Override
    public void start() {
        for (Action action : actions) {
            action.start();
        }
    }

    @Override
    public @NotNull Optional<ZombiesPlayerState> tick(long time) {
        for (Action action : actions) {
            action.tick(time);
        }
        return Optional.empty();
    }

    @Override
    public void end() {
        for (Action action : actions) {
            action.end();
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return displayName;
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    public interface Action {

        default void start() {

        }

        default void tick(long time) {

        }

        default void end() {

        }


    }

}
