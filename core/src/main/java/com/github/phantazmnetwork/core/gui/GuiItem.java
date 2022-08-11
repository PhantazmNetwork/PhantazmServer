package com.github.phantazmnetwork.core.gui;

import com.github.phantazmnetwork.core.inventory.InventoryObject;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * An interaction-capable item in a GUI. When present in a dynamic GUI, may be animated (its tick method will be called
 * to update its {@link ItemStack} if necessary, which will then be retrieved through getStack).
 */
public interface GuiItem extends ClickHandler, RemoveHandler, ReplaceHandler, InventoryObject {
    enum ClickType {
        /**
         * Represents a left click.
         */
        LEFT_CLICK,

        /**
         * Represents a right click.
         */
        RIGHT_CLICK,

        /**
         * Represents a shift click.
         */
        SHIFT_CLICK,

        /**
         * Represents a middle click.
         */
        MIDDLE_CLICK,

        /**
         * Represents a double click.
         */
        DOUBLE_CLICK
    }

    /**
     * Builder for a GuiItem implementation.
     */
    class Builder {
        private final Collection<ClickHandler> clickHandlers;
        private final Collection<RemoveHandler> removeHandlers;
        private final Collection<ReplaceHandler> replaceHandlers;
        private ItemStack itemStack = ItemStack.AIR;
        private ItemUpdater updater = null;

        private Builder() {
            this.clickHandlers = new ArrayList<>(1);
            this.removeHandlers = new ArrayList<>(1);
            this.replaceHandlers = new ArrayList<>(1);
        }

        /**
         * Adds a {@link ClickHandler} to this builder.
         *
         * @param clickHandler the ClickHandler to add
         * @return this instance, for chaining
         */
        public @NotNull Builder withClickHandler(@NotNull ClickHandler clickHandler) {
            clickHandlers.add(Objects.requireNonNull(clickHandler, "clickHandler"));
            return this;
        }

        /**
         * Adds a {@link RemoveHandler} to this builder.
         *
         * @param removeHandler the RemoveHandler to add
         * @return this instance, for chaining
         */
        public @NotNull Builder withRemoveHandler(@NotNull RemoveHandler removeHandler) {
            removeHandlers.add(Objects.requireNonNull(removeHandler, "removeHandler"));
            return this;
        }

        /**
         * Adds a {@link ReplaceHandler} to this builder.
         *
         * @param replaceHandler the ReplaceHandler to add
         * @return this instance, for chaining
         */
        public @NotNull Builder withReplaceHandler(@NotNull ReplaceHandler replaceHandler) {
            replaceHandlers.add(Objects.requireNonNull(replaceHandler, "replaceHandler"));
            return this;
        }

        /**
         * Specifies the initial {@link ItemStack}. This may be dynamically updated by specifying an {@link ItemUpdater}
         * in {@link Builder#withUpdater(ItemUpdater)}.
         *
         * @param itemStack the initial ItemStack
         * @return this instance, for chaining
         */
        public @NotNull Builder withItem(@NotNull ItemStack itemStack) {
            this.itemStack = Objects.requireNonNull(itemStack, "itemStack");
            return this;
        }

        /**
         * Specifies an {@link ItemUpdater} which can be used to periodically update the {@link ItemStack} representing
         * this GuiItem.
         *
         * @param updater the ItemUpdater to use
         * @return this instance, for chaining
         */
        public @NotNull Builder withUpdater(@NotNull ItemUpdater updater) {
            this.updater = Objects.requireNonNull(updater);
            return this;
        }

        /**
         * Builds a new {@link GuiItem} instance. Can be called multiple times to produce distinct {@link GuiItem}s
         * that use the same handlers.
         *
         * @return a new GuiItem instance
         */
        public @NotNull GuiItem build() {
            return new GuiItem() {
                private boolean redraw = false;

                @Override
                public @NotNull ItemStack getItemStack() {
                    redraw = false;
                    return stack;
                }

                @Override
                public boolean shouldRedraw() {
                    return redraw;
                }

                private ItemStack stack = Builder.this.itemStack;

                @Override
                public void handleClick(@NotNull Gui owner, @NotNull Player player, int slot,
                        @NotNull ClickType clickType) {
                    for (ClickHandler clickHandler : clickHandlers) {
                        clickHandler.handleClick(owner, player, slot, clickType);
                    }
                }

                @Override
                public void onRemove(@NotNull Gui owner, int slot) {
                    for (RemoveHandler removeHandler : removeHandlers) {
                        removeHandler.onRemove(owner, slot);
                    }
                }

                @Override
                public void onReplace(@NotNull Gui owner, @NotNull GuiItem newItem, int slot) {
                    for (ReplaceHandler replaceHandler : replaceHandlers) {
                        replaceHandler.onReplace(owner, newItem, slot);
                    }
                }

                @Override
                public void tick(long time) {
                    if (updater != null && updater.hasUpdate(time, stack)) {
                        stack = updater.update(time, stack);
                        redraw = true;
                    }
                }
            };
        }
    }

    /**
     * Returns a new GuiItem builder.
     *
     * @return a new instance of the builder class
     */
    static Builder builder() {
        return new Builder();
    }
}
