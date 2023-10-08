package org.phantazm.zombies.npc;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.commons.MonoComponent;
import org.phantazm.core.npc.interactor.NPCInteractor;

public class ModifierGuiInteractor implements MonoComponent<@NotNull NPCInteractor> {
    @Override
    public @NotNull NPCInteractor apply(@NotNull InjectionStore injectionStore) {
        return null;
    }

    private static class Impl implements NPCInteractor {

        @Override
        public void interact(@NotNull Player player) {

        }
    }
}
