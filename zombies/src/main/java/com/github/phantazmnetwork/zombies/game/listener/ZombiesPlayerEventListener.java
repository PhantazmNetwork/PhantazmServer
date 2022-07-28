package com.github.phantazmnetwork.zombies.game.listener;

import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class ZombiesPlayerEventListener<TEvent extends PlayerInstanceEvent> implements Consumer<TEvent> {

    private final Instance instance;

    private final Map<UUID, ZombiesPlayer> zombiesPlayers;

    public ZombiesPlayerEventListener(@NotNull Instance instance, @NotNull Map<UUID, ZombiesPlayer> zombiesPlayers) {
        this.instance = Objects.requireNonNull(instance, "instance");
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
    }

    @Override
    public void accept(TEvent event) {
        if (event.getInstance() != instance) {
            return;
        }

        ZombiesPlayer zombiesPlayer = zombiesPlayers.get(event.getPlayer().getUuid());
        if (zombiesPlayer != null) {
            accept(zombiesPlayer, event);
        }
    }

    protected void accept(@NotNull ZombiesPlayer zombiesPlayer, @NotNull TEvent event) {

    }

}
