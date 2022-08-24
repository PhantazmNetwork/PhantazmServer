package com.github.phantazmnetwork.zombies.game.player.state;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.game.player.state.context.DeadPlayerStateContext;
import com.github.phantazmnetwork.zombies.game.player.state.context.KnockedPlayerStateContext;
import com.github.phantazmnetwork.zombies.game.player.state.context.NoContext;
import net.kyori.adventure.key.Key;

public class ZombiesPlayerStateKeys {

    public static final PlayerStateKey<NoContext> ALIVE =
            new PlayerStateKey<>(Key.key(Namespaces.PHANTAZM, "zombies.player.state.alive"));

    public static final PlayerStateKey<KnockedPlayerStateContext> KNOCKED =
            new PlayerStateKey<>(Key.key(Namespaces.PHANTAZM, "zombies.player.state.knocked"));

    public static final PlayerStateKey<DeadPlayerStateContext> DEAD =
            new PlayerStateKey<>(Key.key(Namespaces.PHANTAZM, "zombies.player.state.dead"));

    public static final PlayerStateKey<NoContext> QUIT =
            new PlayerStateKey<>(Key.key(Namespaces.PHANTAZM, "zombies.player.state.quit"));

    private ZombiesPlayerStateKeys() {
        throw new UnsupportedOperationException();
    }

}
