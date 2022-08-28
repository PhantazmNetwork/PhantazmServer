package com.github.phantazmnetwork.zombies.game.map.shop.gui;

import com.github.phantazmnetwork.core.gui.ItemUpdater;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@Model("zombies.map.shop.gui.item_updater.static")
public class StaticItemUpdater implements ItemUpdater {
    @FactoryMethod
    public StaticItemUpdater() {
    }

    @Override
    public @NotNull ItemStack update(long time, @NotNull ItemStack current) {
        return current;
    }

    @Override
    public boolean hasUpdate(long time, @NotNull ItemStack current) {
        return false;
    }
}
