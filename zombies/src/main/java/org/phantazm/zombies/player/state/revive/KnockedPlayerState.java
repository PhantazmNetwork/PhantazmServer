package org.phantazm.zombies.player.state.revive;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.tick.Activable;
import org.phantazm.zombies.event.player.ZombiesPlayerReviveEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.ZombiesPlayerState;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class KnockedPlayerState implements ZombiesPlayerState {
    private static final Component DISPLAY_NAME = Component.text("REVIVE").color(NamedTextColor.YELLOW);

    private final ZombiesScene zombiesScene;
    private final ReviveHandler reviveHandler;

    private final Collection<Activable> activables;
    private final Supplier<ZombiesPlayer> self;

    public KnockedPlayerState(@NotNull ZombiesScene zombiesScene, @NotNull Supplier<ZombiesPlayer> self, @NotNull ReviveHandler reviveHandler, @NotNull Collection<Activable> activables) {
        this.zombiesScene = Objects.requireNonNull(zombiesScene);
        this.reviveHandler = Objects.requireNonNull(reviveHandler);
        this.activables = Objects.requireNonNull(activables);
        this.self = Objects.requireNonNull(self);
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

        Optional<ZombiesPlayerState> state = reviveHandler.getSuggestedState();
        if (state.isPresent() && state.get().key().equals(ZombiesPlayerStateKeys.ALIVE.key())) {
            Optional<ZombiesPlayer> reviverOptional = reviveHandler.getReviver();
            if (reviverOptional.isEmpty()) {
                return state;
            }


            ZombiesPlayer reviver = reviverOptional.get();
            ZombiesPlayer reviveTarget = self.get();

            reviver.getPlayer().ifPresent(player -> zombiesScene
                .broadcastEvent(new ZombiesPlayerReviveEvent(player, reviver, reviveTarget)));
        }

        return state;
    }

    @Override
    public void end() {
        reviveHandler.setReviver(null);
        reviveHandler.end();
        for (Activable activable : activables) {
            activable.end();
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public @NotNull Key key() {
        return ZombiesPlayerStateKeys.KNOCKED.key();
    }

}
