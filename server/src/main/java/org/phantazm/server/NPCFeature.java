package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.npc.EntityNPC;
import org.phantazm.core.npc.MobEntitySupplier;
import org.phantazm.core.npc.join.CommandInteractor;

public class NPCFeature {
    private NPCFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull ContextManager contextManager) {
        contextManager.registerElementClass(CommandInteractor.class);
        contextManager.registerElementClass(EntityNPC.class);
        contextManager.registerElementClass(MobEntitySupplier.class);
    }
}
