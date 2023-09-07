package org.phantazm.zombies.player;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;

public interface PlayerComponent<TObject> {

    @NotNull TObject forPlayer(@NotNull ZombiesPlayer player, @NotNull InjectionStore injectionStore);

}
