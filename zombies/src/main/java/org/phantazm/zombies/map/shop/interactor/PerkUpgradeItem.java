package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.gui.Gui;
import org.phantazm.core.item.UpdatingItem;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.function.Supplier;

@Model("item.updating.perk_upgrade")
@Cache(false)
public class PerkUpgradeItem implements UpdatingItem {
    private static final int UPDATE_INTERVAL = 10;

    private final Data data;
    private final Supplier<ZombiesScene> sceneSupplier;

    private ItemStack itemStack;
    private int ticks;

    @FactoryMethod
    public PerkUpgradeItem(@NotNull Data data, @NotNull Supplier<ZombiesScene> sceneSupplier) {
        this.data = data;
        this.sceneSupplier = sceneSupplier;
    }

    @Override
    public @NotNull ItemStack update(@NotNull Gui gui, long time, @NotNull ItemStack current) {
        return itemStack;
    }

    @Override
    public boolean hasUpdate(@NotNull Gui gui, long time, @NotNull ItemStack current) {
        return (ticks++ % UPDATE_INTERVAL == 0);
    }

    @Override
    public @NotNull ItemStack currentItem() {
        return itemStack;
    }

    @DataObject
    public record Data() {
    }
}
