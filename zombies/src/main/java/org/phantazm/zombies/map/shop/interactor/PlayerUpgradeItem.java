package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.gui.Gui;
import org.phantazm.core.gui.ItemUpdater;
import org.phantazm.zombies.ZombiesTagUtils;
import org.phantazm.zombies.player.upgrade.PlayerUpgradeHandler;
import org.phantazm.zombies.player.upgrade.UpgradeActivatorComponent;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.function.Supplier;

@Model("item.updating.player_upgrade")
@Cache(false)
public class PlayerUpgradeItem implements ItemUpdater {
    private final Data data;
    private final ItemUpdater ineligibleItem;
    private final ItemUpdater purchasedItem;
    private final ItemUpdater unpurchasedItem;
    private final Supplier<ZombiesScene> sceneSupplier;

    private enum UpgradeState {
        Ineligible,
        Unpurchased,
        Purchased
    }

    @FactoryMethod
    public PlayerUpgradeItem(@NotNull Data data,
        @NotNull Supplier<ZombiesScene> sceneSupplier,
        @NotNull @Child("ineligible") ItemUpdater ineligibleItem,
        @NotNull @Child("purchased") ItemUpdater purchasedItem,
        @NotNull @Child("unpurchased") ItemUpdater unpurchasedItem) {
        this.data = data;
        this.sceneSupplier = sceneSupplier;
        this.ineligibleItem = ineligibleItem;
        this.purchasedItem = purchasedItem;
        this.unpurchasedItem = unpurchasedItem;
    }

    @Override
    public @NotNull ItemStack update(@NotNull Gui gui, long time, @NotNull ItemStack current, int slot) {
        return currentUpdater(gui).update(gui, time, current, slot);
    }

    @Override
    public boolean hasUpdate(@NotNull Gui gui, long time, @NotNull ItemStack current, int slot) {
        return currentUpdater(gui).hasUpdate(gui, time, current, slot);
    }

    private ItemUpdater currentUpdater(Gui gui) {
        UpgradeState currentState = computeState(gui);

        return switch (currentState) {
            case Ineligible -> ineligibleItem;
            case Unpurchased -> unpurchasedItem;
            case Purchased -> purchasedItem;
        };
    }

    private PlayerUpgradeHandler handlerFromGui(Gui gui) {
        Player owner = gui.getOwner();
        return sceneSupplier.get().upgradeHandler(owner.getUuid());
    }

    private UpgradeState computeState(Gui gui) {
        PlayerUpgradeHandler handler = handlerFromGui(gui);
        ZombiesScene scene = handler.zombiesPlayer().getScene();
        TagHandler localTags = ZombiesTagUtils.sceneLocalTags(scene, gui.getOwner());
        UpgradeActivatorComponent activatorComponent = scene.upgradeActivatorComponent();

        if (!activatorComponent.mayPurchase(data.upgrade, localTags, data.isSynergy)) {
            return UpgradeState.Ineligible;
        }

        if (localTags.getTag(activatorComponent.purchaseTag(data.upgrade))) {
            return UpgradeState.Purchased;
        }

        return UpgradeState.Unpurchased;
    }

    @DataObject
    @Default("""
        {
          isSynergy=false
        }
        """)
    public record Data(@NotNull Key upgrade,
        boolean isSynergy) {

    }
}
