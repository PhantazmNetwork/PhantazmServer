package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.gui.Gui;
import org.phantazm.core.item.UpdatingItem;
import org.phantazm.zombies.ZombiesTagUtils;
import org.phantazm.zombies.player.upgrade.PlayerUpgradeHandler;
import org.phantazm.zombies.player.upgrade.UpgradeActivatorComponent;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.function.Supplier;

@Model("item.updating.player_upgrade")
@Cache(false)
public class PlayerUpgradeItem implements UpdatingItem {
    private final Data data;
    private final UpdatingItem ineligibleItem;
    private final UpdatingItem purchasedItem;
    private final UpdatingItem unpurchasedItem;
    private final Supplier<ZombiesScene> sceneSupplier;

    private UpdatingItem current;
    private UpgradeState lastState;

    private enum UpgradeState {
        Ineligible,
        Unpurchased,
        Purchased
    }

    @FactoryMethod
    public PlayerUpgradeItem(@NotNull Data data,
        @NotNull Supplier<ZombiesScene> sceneSupplier,
        @NotNull @Child("ineligible") UpdatingItem ineligibleItem,
        @NotNull @Child("purchased") UpdatingItem purchasedItem,
        @NotNull @Child("unpurchased") UpdatingItem unpurchasedItem) {
        this.data = data;
        this.sceneSupplier = sceneSupplier;
        this.ineligibleItem = ineligibleItem;
        this.purchasedItem = purchasedItem;
        this.unpurchasedItem = unpurchasedItem;
    }

    @Override
    public @NotNull ItemStack update(@NotNull Gui gui, long time, @NotNull ItemStack current) {
        return computeItemStack(gui, time, current);
    }

    @Override
    public boolean hasUpdate(@NotNull Gui gui, long time, @NotNull ItemStack current) {
        UpdatingItem thisCurrent = this.current;
        return thisCurrent == null || thisCurrent.hasUpdate(gui, time, current) || upgradeChanged(gui);
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

    private boolean upgradeChanged(Gui gui) {
        return this.lastState != computeState(gui);
    }

    private ItemStack computeItemStack(Gui gui, long time, ItemStack current) {
        UpgradeState currentState = computeState(gui);
        this.lastState = currentState;

        return switch (currentState) {
            case Ineligible -> {
                this.current = ineligibleItem;
                yield ineligibleItem.update(gui, time, current);
            }
            case Unpurchased -> {
                this.current = unpurchasedItem;
                yield unpurchasedItem.update(gui, time, current);
            }
            case Purchased -> {
                this.current = purchasedItem;
                yield purchasedItem.update(gui, time, current);
            }
        };
    }

    @Override
    public @NotNull ItemStack currentItem() {
        UpdatingItem current = this.current;
        if (current == null) return ItemStack.AIR;

        return current.currentItem();
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
