package org.phantazm.core.npc.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

@Model("npc.interactor.none")
@Cache
public class NoInteractor implements Interactor {
    @FactoryMethod
    public NoInteractor() {
    }

    @Override
    public void interact(@NotNull Player player) {

    }
}
