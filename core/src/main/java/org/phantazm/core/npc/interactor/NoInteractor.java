package org.phantazm.core.npc.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.MonoComponent;
import org.phantazm.commons.InjectionStore;

@Model("npc.interactor.none")
@Cache
public class NoInteractor implements MonoComponent<@NotNull NPCInteractor> {
    private static final NPCInteractor INSTANCE = player -> {
    };

    @FactoryMethod
    public NoInteractor() {
    }

    @Override
    public @NotNull NPCInteractor apply(@NotNull InjectionStore injectionStore) {
        return INSTANCE;
    }
}
