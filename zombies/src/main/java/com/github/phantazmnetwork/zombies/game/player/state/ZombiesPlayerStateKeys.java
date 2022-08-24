package com.github.phantazmnetwork.zombies.game.player.state;

import com.github.phantazmnetwork.commons.Namespaces;
import net.kyori.adventure.key.Key;

public class ZombiesPlayerStateKeys {

    public static final Key ALIVE = Key.key(Namespaces.PHANTAZM, "zombies.player.state.alive");

    public static final Key KNOCKED = Key.key(Namespaces.PHANTAZM, "zombies.player.state.knocked");

    public static final Key DEAD = Key.key(Namespaces.PHANTAZM, "zombies.player.state.dead");

    public static final Key QUIT = Key.key(Namespaces.PHANTAZM, "zombies.player.state.quit");

    private ZombiesPlayerStateKeys() {
        throw new UnsupportedOperationException();
    }

}
