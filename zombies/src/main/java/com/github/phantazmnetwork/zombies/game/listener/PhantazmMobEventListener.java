package com.github.phantazmnetwork.zombies.game.listener;

import com.github.phantazmnetwork.zombies.game.event.PhantazmMobEvent;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

public interface PhantazmMobEventListener<TEvent extends Event> {

    void accept(@NotNull PhantazmMobEvent<TEvent> event);

}
