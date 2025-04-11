package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.gui.Gui;
import org.phantazm.core.item.UpdatingItem;
import org.phantazm.zombies.player.upgrade.PlayerUpgradeHandler;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.function.Supplier;

@Model("item.updating.player_upgrade")
@Cache(false)
public class PlayerUpgradeItem implements UpdatingItem {
    private final Data data;
    private final UpdatingItem purchasedItem;
    private final UpdatingItem unpurchasedItem;
    private final Supplier<ZombiesScene> sceneSupplier;

    private UpdatingItem current;

    @FactoryMethod
    public PlayerUpgradeItem(@NotNull Data data,
        @NotNull Supplier<ZombiesScene> sceneSupplier,
        @NotNull @Child("purchased") UpdatingItem purchasedItem,
        @NotNull @Child("unpurchased") UpdatingItem unpurchasedItem) {
        this.data = data;
        this.sceneSupplier = sceneSupplier;
        this.purchasedItem = purchasedItem;
        this.unpurchasedItem = unpurchasedItem;
    }

    @Override
    public @NotNull ItemStack update(@NotNull Gui gui, long time, @NotNull ItemStack current) {
        return computeItemStack(gui, time, current);
    }

    @Override
    public boolean hasUpdate(@NotNull Gui gui, long time, @NotNull ItemStack current) {
        return this.current == null || this.current.hasUpdate(gui, time, current);
    }

    private ItemStack computeItemStack(Gui gui, long time, ItemStack current) {
        Player owner = gui.getOwner();
        PlayerUpgradeHandler upgradeHandler = sceneSupplier.get().upgradeHandler(owner.getUuid());

        if (upgradeHandler.isUpgradeActive(data.upgrade)) {
            this.current = this.purchasedItem;
            return this.purchasedItem.update(gui, time, current);
        } else {
            this.current = this.unpurchasedItem;
            return this.unpurchasedItem.update(gui, time, current);
        }
    }

    @Override
    public @NotNull ItemStack currentItem() {
        UpdatingItem current = this.current;
        if (current == null) return ItemStack.AIR;

        return current.currentItem();
    }

    @DataObject
    public record Data(@NotNull Key upgrade) {

    }
}
