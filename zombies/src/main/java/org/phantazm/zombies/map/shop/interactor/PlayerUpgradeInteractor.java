package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.player.upgrade.PlayerUpgradeHandler;
import org.phantazm.zombies.scene2.ZombiesScene;
import net.kyori.adventure.key.Key;

@Model("zombies.map.shop.interactor.player_upgrade")
public class PlayerUpgradeInteractor implements ShopInteractor {
    private final Data data;

    @FactoryMethod
    public PlayerUpgradeInteractor(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        ZombiesScene scene = interaction.player().getScene();
        PlayerUpgradeHandler handler = scene.upgradeHandler(interaction.player().getUUID());
        return handler.activateUpgrade(data.equipmentKey);
    }

    @DataObject
    public record Data(@NotNull Key equipmentKey) {
    }
}
