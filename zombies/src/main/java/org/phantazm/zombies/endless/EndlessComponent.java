package org.phantazm.zombies.endless;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.DualComponent;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.map.Round;
import org.phantazm.zombies.scene2.ZombiesScene;

public class EndlessComponent implements DualComponent<ZombiesScene, EndlessHandler> {

    @Override
    public @NotNull EndlessHandler apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesScene zombiesScene) {
        return null;
    }

    private static class Impl implements EndlessHandler {

        @Override
        public @NotNull Round generateRound(int round) {
            return null;
        }
    }
}
