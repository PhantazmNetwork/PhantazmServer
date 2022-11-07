package com.github.phantazmnetwork.zombies.game.listener;

import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public abstract class ZombiesPlayerEventListener<TEvent extends EntityInstanceEvent> implements Consumer<TEvent> {

    private final Instance instance;

    private final Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers;

    public ZombiesPlayerEventListener(@NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers) {
        this.instance = Objects.requireNonNull(instance, "instance");
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
    }

    @Override
    public void accept(TEvent event) {
        if (event.getInstance() != instance) {
            return;
        }

        ZombiesPlayer zombiesPlayer = zombiesPlayers.get(event.getEntity().getUuid());
        if (zombiesPlayer != null) {
            accept(zombiesPlayer, event);
        }
    }

    protected abstract void accept(@NotNull ZombiesPlayer zombiesPlayer, @NotNull TEvent event);

}
