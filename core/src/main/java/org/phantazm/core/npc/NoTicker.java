package org.phantazm.core.npc;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.BasicComponent;
import org.phantazm.commons.InjectionStore;

@Model("npc.entity.ticker.none")
@Cache
public class NoTicker implements BasicComponent<EntityTicker> {
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
