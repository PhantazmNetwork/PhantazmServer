package com.github.phantazmnetwork.zombies.game.listener;

import com.github.phantazmnetwork.zombies.game.event.ZombiesPlayerEvent;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

public interface ZombiesPlayerEventListener<TEvent extends Event> {

    void accept(@NotNull ZombiesPlayerEvent<TEvent> event);

}
