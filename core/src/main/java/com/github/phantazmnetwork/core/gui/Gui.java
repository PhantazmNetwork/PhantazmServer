package com.github.phantazmnetwork.core.gui;

import com.github.phantazmnetwork.commons.Tickable;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiPredicate;

/**
 * Extension of {@link Inventory} designed to ease the creation of graphical user interfaces. May or may not be
 * "dynamic". Dynamic GUIs support animations and tick all of their constituent {@link GuiItem}s.
 */
@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
public class Gui extends Inventory implements Tickable {
    /**
     * Builder for Gui instances.
     */
    public static class Builder {
        private final List<GuiItem> items;
        private final InventoryType type;
        private final SlotDistributor slotDistributor;
        private Component title = Component.empty();
        private boolean dynamic = false;

        private Builder(@NotNull InventoryType type, @NotNull SlotDistributor slotDistributor) {
            this.items = new ArrayList<>();
            this.type = Objects.requireNonNull(type, "type");
            this.slotDistributor = Objects.requireNonNull(slotDistributor, "slotDistributor");
        }

        /**
         * Specifies a title for the Gui.
         *
         * @param component the title {@link Component}
         * @return this instance, for chaining
         */
        public @NotNull Builder withTitle(@NotNull Component component) {
            this.title = Objects.requireNonNull(component, "component");
            return this;
        }

        /**
         * Specifies whether the Gui will be dynamic. Default false.
         *
         * @param dynamic sets whether the Gui will be dynamic
         * @return this instance, for chaining
         */
        public @NotNull Builder setDynamic(boolean dynamic) {
            this.dynamic = dynamic;
            return this;
        }

        /**
         * Adds an item to the Gui. Will be positioned by the {@link SlotDistributor} when built. If there are more
         * items than can be held by the Gui, throws an exception.
         *
         * @param item the item to add
         * @return this instance, for chaining
         */
        public @NotNull Builder withItem(@NotNull GuiItem item) {
            items.add(Objects.requireNonNull(item, "item"));
            if (items.size() > type.getSize()) {
                throw new IllegalArgumentException("too many items for InventoryType " + type);
            }

            return this;
        }

        /**
         * Adds a collection of items to the Gui. Will be positioned by the {@link SlotDistributor} when built. If there
         * are more items than can be held by the Gui, throws an exception.
         *
         * @param items the items to add
         * @return this instance, for chaining
         */
        public @NotNull Builder withItems(@NotNull Collection<? extends GuiItem> items) {
            this.items.addAll(items);
            if (items.size() > type.getSize()) {
                throw new IllegalArgumentException("too many items for InventoryType " + type);
            }

            return this;
        }

        /**
         * Builds the Gui, positioning items according to the {@link SlotDistributor} used by this builder.
         *
         * @return a new Gui
         */
        public @NotNull Gui build() {
            int size = type.getSize();

            int width = switch (type) {
                case CHEST_1_ROW, CHEST_2_ROW, CHEST_3_ROW, CHEST_4_ROW, CHEST_5_ROW, CHEST_6_ROW, SHULKER_BOX -> 9;
                case WINDOW_3X3 -> 3;
                default -> size;
            };

            int height = size / width;

            int[] slots = slotDistributor.distribute(width, height, items.size());

            Gui gui = new Gui(type, title, dynamic);
            for (int i = 0; i < slots.length; i++) {
                gui.insertItem(items.get(i), slots[i]);
            }

            return gui;
        }
    }

    /**
     * Creates a new instance of the builder class which may be used to more easily construct Gui instances.
     *
     * @param inventoryType   the type of inventory used by the Gui
     * @param slotDistributor the {@link SlotDistributor} used to position items
     * @return the builder instance
     */
    public static @NotNull Builder builder(@NotNull InventoryType inventoryType,
            @NotNull SlotDistributor slotDistributor) {
        return new Builder(inventoryType, slotDistributor);
    }

    /**
     * A {@link GuiItem} in a particular slot. Used by methods like {@link Gui#getItems()} to retain slot information.
     *
     * @param item the GuiItem
     * @param slot the slot it's in
     */
    public record SlottedItem(@NotNull GuiItem item, int slot) {
    }

    private final GuiItem[] items;
    private final boolean isDynamic;

    /**
     * Creates a new instance of this class.
     *
     * @param inventoryType the type of inventory to send
     * @param title         the inventory's title text
     * @param isDynamic     whether this GUI is "dynamic"
     */
    public Gui(@NotNull InventoryType inventoryType, @NotNull Component title, boolean isDynamic) {
        super(inventoryType, title);
        items = new GuiItem[inventoryType.getSize()];
        this.isDynamic = isDynamic;
    }

    /**
     * Inserts a {@link GuiItem} into the specified slot. Calls {@link GuiItem#onReplace(Gui, GuiItem, int)} on the
     * previous item, if any is present.
     *
     * @param item the new item
     * @param slot the slot to insert it at
     */
    public void insertItem(@NotNull GuiItem item, int slot) {
        Objects.requireNonNull(item, "item");

        safeItemInsert(slot, item.getItemStack(), true);
        GuiItem oldItem = items[slot];

        if (oldItem != null) {
            synchronized (oldItem) {
                items[slot] = null;
                oldItem.onReplace(this, item, slot);
            }
        }
        else {
            items[slot] = item;
        }
    }

    /**
     * Removes a {@link GuiItem} from the specified slot. Calls {@link GuiItem#onRemove(Gui, int)} on the previous
     * item, if any is present.
     *
     * @param slot the slot to remove the item from
     */
    public void removeItem(int slot) {
        GuiItem oldItem = items[slot];
        if (oldItem == null) {
            throw new IllegalArgumentException("tried to remove item in slot " + slot + ", but none exists");
        }

        synchronized (oldItem) {
            items[slot] = null;
            oldItem.onRemove(this, slot);
        }
    }

    /**
     * Optionally retrieves a {@link GuiItem} from the specific slot. If none exists, the returned optional will be
     * empty.
     *
     * @param slot the slot to retrieve the item from
     * @return an optional which may contain the GuiItem
     */
    public @NotNull Optional<GuiItem> itemAt(int slot) {
        return Optional.ofNullable(items[slot]);
    }

    /**
     * Returns a new, mutable list containing {@link SlottedItem}s for each {@link GuiItem} present in this Gui.
     *
     * @return a mutable list of SlottedItems
     */
    public @NotNull List<SlottedItem> getItems() {
        ArrayList<SlottedItem> slottedItems = new ArrayList<>(items.length);
        for (int i = 0; i < items.length; i++) {
            GuiItem item = items[i];
            if (item != null) {
                slottedItems.add(new SlottedItem(item, i));
            }
        }

        return slottedItems;
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
        if (!isDynamic) {
            //non-dynamic GUI's don't update their items
            return;
        }

        for (int i = 0; i < items.length; i++) {
            //harmless race condition, tick thread does not write unless synchronizing on GuiItem
            GuiItem item = items[i];

            if (item != null) {
                //TODO: investigate potential deadlock conditions when tick thread locks on the GuiItem
                synchronized (item) {
                    item.tick(time);
                    if (item.shouldRedraw()) {
                        insertItem(item, i);
                    }
                }
            }
        }
    }

    private boolean handleClick(Player player, int slot, BiPredicate<? super Player, Integer> superFunction,
            GuiItem.ClickType clickType) {
        if (slot < getInventoryType().getSize()) {
            GuiItem item = items[slot];
            if (item != null) {
                synchronized (item) {
                    item.handleClick(this, player, slot, clickType);
                    return false;
                }
            }
        }

        return superFunction.test(player, slot);
    }
}
