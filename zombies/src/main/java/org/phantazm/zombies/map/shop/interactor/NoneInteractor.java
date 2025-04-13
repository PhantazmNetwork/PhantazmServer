package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;

@Model("zombies.map.shop.interactor.none")
@Cache
public class NoneInteractor implements ShopInteractor {
    @FactoryMethod
    public NoneInteractor() {
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        return true;
    }
}
