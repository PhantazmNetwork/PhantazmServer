package org.phantazm.core.gui;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Tickable;

import java.util.*;
import java.util.function.BiPredicate;

/**
 * Extension of {@link Inventory} designed to ease the creation of graphical user interfaces. May or may not be
 * "dynamic". Dynamic GUIs support animations and tick all of their constituent {@link GuiItem}s.
 */
@SuppressWarnings({"SynchronizationOnLocalVariableOrMethodParameter", "unchecked"})
public class Gui extends Inventory implements Tickable {
    private final Int2ObjectMap.Entry<GuiItem>[] EMPTY_INT_2_OBJECT_MAP_ENTRY_ARRAY = new Int2ObjectMap.Entry[0];

    private final Int2ObjectMap<GuiItem> items;
    private final boolean isDynamic;
    //cached array of items, indirectly used by the tick function, set to null whenever the map is written to
    private volatile SlottedItem[] tickItems;

    /**
     * Creates a new instance of this class.
     *
     * @param inventoryType the type of inventory to send
     * @param title         the inventory's title text
     * @param isDynamic     whether this GUI is "dynamic" (supports animated {@link GuiItem}s and updates them as
     *                      needed)
     */
    public Gui(@NotNull InventoryType inventoryType, @NotNull Component title, boolean isDynamic) {
        super(inventoryType, title);
        items = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>(inventoryType.getSize() >> 2));
        this.isDynamic = isDynamic;
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
     * Inserts a {@link GuiItem} into the specified slot. Calls {@link GuiItem#onReplace(Gui, GuiItem, int)} on the
     * previous item, if any is present.
     *
     * @param item the new item
     * @param slot the slot to insert it at
     */
    public void insertItem(@NotNull GuiItem item, int slot) {
        Objects.requireNonNull(item, "item");
        safeItemInsert(slot, item.getItemStack(), true);

        items.put(slot, item);
        tickItems = null;

        GuiItem oldItem = items.get(slot);
        if (oldItem != null) {
            synchronized (oldItem) {
                oldItem.onReplace(this, item, slot);
            }
        }
    }

    /**
     * Removes a {@link GuiItem} from the specified slot. Calls {@link GuiItem#onRemove(Gui, int)} on the previous
     * item, if any is present.
     *
     * @param slot the slot to remove the item from
     */
    public void removeItem(int slot) {
        GuiItem oldItem = items.get(slot);
        if (oldItem == null) {
            return;
        }

        items.remove(slot);
        tickItems = null;

        synchronized (oldItem) {
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
        return Optional.ofNullable(items.get(slot));
    }

    /**
     * Returns a new, mutable list containing {@link SlottedItem}s for each {@link GuiItem} present in this Gui.
     *
     * @return a mutable list of SlottedItems
     */
    public @NotNull List<SlottedItem> getItems() {
        List<SlottedItem> slottedItems = new ArrayList<>(items.size());
        for (Int2ObjectMap.Entry<GuiItem> entry : items.int2ObjectEntrySet()) {
            slottedItems.add(new SlottedItem(entry.getValue(), entry.getIntKey()));
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

        SlottedItem[] tickItems = getTickItems();
        for (SlottedItem slottedItem : tickItems) {
            GuiItem item = slottedItem.item;

            item.tick(time);
            if (item.shouldRedraw()) {
                setItemStack(slottedItem.slot, item.getItemStack());
            }
        }
    }

    private SlottedItem[] getTickItems() {
        //create copy in case this.tickItems is set to null after we check for it
        SlottedItem[] tickItems = this.tickItems;

        if (tickItems == null) {
            //create copy of entry set, otherwise it could change while we iterate
            Int2ObjectMap.Entry<GuiItem>[] entries =
                    items.int2ObjectEntrySet().toArray(EMPTY_INT_2_OBJECT_MAP_ENTRY_ARRAY);

            SlottedItem[] newTickItems = new SlottedItem[entries.length];
            for (int i = 0; i < newTickItems.length; i++) {
                Int2ObjectMap.Entry<GuiItem> entry = entries[i];
                newTickItems[i] = new SlottedItem(entry.getValue(), entry.getIntKey());
            }

            return newTickItems;
        }

        return tickItems;
    }

    private boolean handleClick(Player player, int slot, BiPredicate<? super Player, Integer> superFunction,
            GuiItem.ClickType clickType) {
        if (slot < getInventoryType().getSize()) {
            GuiItem item = items.get(slot);
            if (item != null) {
                synchronized (item) {
                    item.handleClick(this, player, slot, clickType);
                    return false;
                }
            }
        }

        return superFunction.test(player, slot);
    }

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
     * A {@link GuiItem} in a particular slot. Used by methods like {@link Gui#getItems()} to retain slot information.
     *
     * @param item the GuiItem
     * @param slot the slot it's in
     */
    public record SlottedItem(@NotNull GuiItem item, int slot) {
    }
}
