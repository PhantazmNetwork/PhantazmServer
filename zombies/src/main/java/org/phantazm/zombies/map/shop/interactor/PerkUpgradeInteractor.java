package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.player.upgrade.PlayerUpgradeHandler;

@Model("zombies.map.shop.interactor.perk_upgrade")
@Cache(false)
public class PerkUpgradeInteractor extends InteractorBase<PerkUpgradeInteractor.Data> {
    @FactoryMethod
    public PerkUpgradeInteractor(@NotNull Data data) {
        super(data);
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        PlayerUpgradeHandler handler = interaction.player().getScene().upgradeHandler(interaction.player().getUUID());
        if (handler == null) return false;

        return handler.activateUpgrade(data.key);
    }

    @DataObject
    public record Data(@NotNull Key key) {
    }
}
