package org.phantazm.core.npc;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.MonoComponent;
import org.phantazm.commons.InjectionStore;

@Model("npc.entity.ticker.none")
@Cache
public class NoTicker implements MonoComponent<EntityTicker> {
    private static final EntityTicker INSTANCE = (time, entity) -> {
    };

    @FactoryMethod
    public NoTicker() {

    }

    @Override
    public EntityTicker apply(@NotNull InjectionStore injectionStore) {
        return INSTANCE;
    }
}
