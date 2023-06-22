package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.npc.AnimationTicker;
import org.phantazm.core.npc.EntityNPC;
import org.phantazm.core.npc.settings.BasicEntitySettings;
import org.phantazm.core.npc.supplier.MobEntitySupplier;
import org.phantazm.core.npc.join.CommandInteractor;
import org.phantazm.core.npc.supplier.PlayerEntitySupplier;

public class NPCFeature {
    private NPCFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull ContextManager contextManager) {
        contextManager.registerElementClass(CommandInteractor.class);
        contextManager.registerElementClass(EntityNPC.class);
        contextManager.registerElementClass(MobEntitySupplier.class);
        contextManager.registerElementClass(PlayerEntitySupplier.class);
        contextManager.registerElementClass(BasicEntitySettings.class);
        contextManager.registerElementClass(AnimationTicker.class);
        contextManager.registerElementClass(AnimationTicker.Frame.class);
    }
}
