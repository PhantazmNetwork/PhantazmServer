package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;

@Model("zombies.map.shop.interactor.close_gui")
public class CloseGuiInteractor implements ShopInteractor {
    @FactoryMethod
    public CloseGuiInteractor() {

    }

    @Override
    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        interaction.player().getModule().getPlayerView().getPlayer().ifPresent(Player::closeInventory);
    }
}
