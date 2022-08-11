package com.github.phantazmnetwork.core.gui;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BasicGuiItem implements GuiItem {
    protected ItemStack stack;

    private final ClickHandler clickHandler;
    private final RemoveHandler removeHandler;
    private final ReplaceHandler replaceHandler;

    public BasicGuiItem(@NotNull ItemStack stack, @NotNull ClickHandler clickHandler,
            @NotNull RemoveHandler removeHandler, @NotNull ReplaceHandler replaceHandler) {
        this.stack = Objects.requireNonNull(stack, "stack");
        this.clickHandler = Objects.requireNonNull(clickHandler, "clickHandler");
        this.removeHandler = Objects.requireNonNull(removeHandler, "removeHandler");
        this.replaceHandler = Objects.requireNonNull(replaceHandler, "replaceHandler");
    }

    @Override
    public void tick(long time) {
    }

    @Override
    public void handleClick(@NotNull Gui owner, int slot, @NotNull ClickType clickType) {
        clickHandler.handleClick(owner, slot, clickType);
    }

    @Override
    public @NotNull ItemStack getStack() {
        return stack;
    }

    @Override
    public void onRemove(@NotNull Gui owner, int slot) {
        removeHandler.onRemove(owner, slot);
    }

    @Override
    public void onReplace(@NotNull Gui owner, @NotNull GuiItem newItem, int slot) {
        replaceHandler.onReplace(owner, newItem, slot);
    }
}
