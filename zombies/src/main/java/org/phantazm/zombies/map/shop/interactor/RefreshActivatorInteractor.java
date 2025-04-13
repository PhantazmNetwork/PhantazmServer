package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;

@Model("zombies.map.shop.interactor.refresh_activator")
@Cache
public class RefreshActivatorInteractor implements ShopInteractor {
    @FactoryMethod
    public RefreshActivatorInteractor() {
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        interaction.player().getScene().upgradeActivator().refresh(interaction.player());
        return true;
    }
}
