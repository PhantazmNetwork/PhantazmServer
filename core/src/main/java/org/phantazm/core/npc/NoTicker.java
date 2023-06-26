package org.phantazm.core.npc;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

@Model("npc.entity.ticker.none")
@Cache
public class NoTicker implements EntityTicker {
    @FactoryMethod
    public NoTicker() {

    }

    @Override
    public void accept(long time, @NotNull Entity entity) {

    }
}
