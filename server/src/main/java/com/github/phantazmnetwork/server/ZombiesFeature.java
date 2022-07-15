package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.commons.component.ComponentBuilder;
import com.github.phantazmnetwork.commons.component.ComponentException;
import com.github.phantazmnetwork.zombies.game.map.action.round.AnnounceRoundAction;
import org.jetbrains.annotations.NotNull;

class ZombiesFeature {
    static void initialize(@NotNull ComponentBuilder builder) throws ComponentException {
        registerComponentClasses(builder);
    }

    private static void registerComponentClasses(ComponentBuilder builder) throws ComponentException {
        builder.registerComponentClass(AnnounceRoundAction.class);
    }
}
