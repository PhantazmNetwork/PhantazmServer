package org.phantazm.zombies.player.state;

import net.kyori.adventure.key.Key;
import org.phantazm.commons.Namespaces;
import org.phantazm.zombies.player.state.context.DeadPlayerStateContext;
import org.phantazm.zombies.player.state.context.QuitPlayerStateContext;
import org.phantazm.zombies.player.state.context.KnockedPlayerStateContext;
import org.phantazm.zombies.player.state.context.NoContext;

public class ZombiesPlayerStateKeys {

    public static final PlayerStateKey<NoContext> ALIVE =
            new PlayerStateKey<>(Key.key(Namespaces.PHANTAZM, "zombies.player.state.alive"));

    public static final PlayerStateKey<KnockedPlayerStateContext> KNOCKED =
            new PlayerStateKey<>(Key.key(Namespaces.PHANTAZM, "zombies.player.state.knocked"));

    public static final PlayerStateKey<DeadPlayerStateContext> DEAD =
            new PlayerStateKey<>(Key.key(Namespaces.PHANTAZM, "zombies.player.state.dead"));

    public static final PlayerStateKey<QuitPlayerStateContext> QUIT =
            new PlayerStateKey<>(Key.key(Namespaces.PHANTAZM, "zombies.player.state.quit"));

    private ZombiesPlayerStateKeys() {
        throw new UnsupportedOperationException();
    }

}
