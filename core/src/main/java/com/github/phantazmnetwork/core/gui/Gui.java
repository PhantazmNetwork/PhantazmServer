package com.github.phantazmnetwork.core.gui;

import com.github.phantazmnetwork.commons.Tickable;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiPredicate;

@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
public class Gui extends Inventory implements Tickable {
    private final GuiItem[] items;

    public Gui(@NotNull InventoryType inventoryType, @NotNull Component title) {
        super(inventoryType, title);
        items = new GuiItem[inventoryType.getSize()];
    }

    public void insertItem(@NotNull GuiItem item, int slot) {
        Objects.requireNonNull(item, "item");

        safeItemInsert(slot, item.getStack(), true);
        GuiItem oldItem = items[slot];

        if (oldItem != null) {
            synchronized (oldItem) {
                items[slot] = null;
                oldItem.onReplace(this, item, slot);
            }
        }
    }

    public void removeItem(int slot) {
        GuiItem oldItem = items[slot];
        if (oldItem == null) {
            throw new IllegalArgumentException("Tried to remove item in slot " + slot + ", but none exists");
        }

        synchronized (oldItem) {
            items[slot] = null;
            oldItem.onRemove(this, slot);
        }
    }

    @Override
    public boolean leftClick(@NotNull Player player, int slot) {
        return handleClick(player, slot, super::leftClick, GuiItem.ClickType.LEFT_CLICK);
    }

    @Override
    public boolean rightClick(@NotNull Player player, int slot) {
        return handleClick(player, slot, super::rightClick, GuiItem.ClickType.RIGHT_CLICK);
    }

    @Override
    public boolean shiftClick(@NotNull Player player, int slot) {
        return handleClick(player, slot, super::shiftClick, GuiItem.ClickType.SHIFT_CLICK);
    }

    @Override
    public boolean middleClick(@NotNull Player player, int slot) {
        return handleClick(player, slot, super::middleClick, GuiItem.ClickType.MIDDLE_CLICK);
    }

    @Override
    public boolean doubleClick(@NotNull Player player, int slot) {
        return handleClick(player, slot, super::doubleClick, GuiItem.ClickType.DOUBLE_CLICK);
    }

    @Override
    public void tick(long time) {
        for (int i = 0; i < items.length; i++) {
            GuiItem item = items[i];
            if (item != null) {
                synchronized (item) {
                    ItemStack oldStack = super.itemStacks[i];
                    if (!item.getStack().equals(oldStack)) {
                        insertItem(item, i);
                    }
                }
            }
        }
    }

    private boolean handleClick(Player player, int slot, BiPredicate<? super Player, Integer> superFunction,
            GuiItem.ClickType clickType) {
        GuiItem item = items[slot];
        if (item != null) {
            synchronized (item) {
                return item.handleClick(this, slot, clickType);
            }
        }

        return superFunction.test(player, slot);
    }
}
