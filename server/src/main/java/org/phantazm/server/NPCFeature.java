package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.npc.EntityJoinNPC;
import org.phantazm.core.npc.join.CommandInteractor;

public class NPCFeature {
    private NPCFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull ContextManager contextManager) {
        contextManager.registerElementClass(CommandInteractor.class);
        contextManager.registerElementClass(EntityJoinNPC.class);
    }
}
