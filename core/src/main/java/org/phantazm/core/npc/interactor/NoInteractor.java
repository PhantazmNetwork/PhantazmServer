package org.phantazm.core.npc.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.BasicComponent;
import org.phantazm.commons.InjectionStore;

@Model("npc.interactor.none")
@Cache
public class NoInteractor implements BasicComponent<Interactor> {
    private static final Interactor INSTANCE = player -> {
    };

    @FactoryMethod
    public NoInteractor() {
    }

    @Override
    public Interactor apply(@NotNull InjectionStore injectionStore) {
        return INSTANCE;
    }
}
