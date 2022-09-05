package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

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
