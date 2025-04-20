package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.gui.Gui;
import org.phantazm.core.gui.GuiItem;
import org.phantazm.core.gui.SlotDistributor;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.map.shop.gui.ClickHandlerBase;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Model("zombies.map.shop.interactor.open_gui")
@Cache(false)
public class OpenGuiInteractor extends InteractorBase<OpenGuiInteractor.Data> {
    private final SlotDistributor slotDistributor;

    private final ArrayList<ClickHandlerBase<?>>[] unfixedItems;
    private final ArrayList<ClickHandlerBase<?>>[] fixedItems;

    private final List<Gui> ticking = new CopyOnWriteArrayList<>();

    @SuppressWarnings("unchecked")
    @FactoryMethod
    public OpenGuiInteractor(@NotNull Data data, @NotNull SlotDistributor slotDistributor,
        @NotNull @Child("guiItems") List<ClickHandlerBase<?>> guiItems) {
        super(data);
        this.slotDistributor = Objects.requireNonNull(slotDistributor);

        int highPage = 0;
        for (ClickHandlerBase<?> item : guiItems) {
            if (item.page() > highPage) {
                highPage = item.page();
            }
        }

        ArrayList<ClickHandlerBase<?>>[] unfixed = (ArrayList<ClickHandlerBase<?>>[]) new ArrayList[highPage + 1];
        ArrayList<ClickHandlerBase<?>>[] fixed = (ArrayList<ClickHandlerBase<?>>[]) new ArrayList[highPage + 1];

        for (int i = 0; i < highPage + 1; i++) {
            int cap = Math.min(guiItems.size(), 10);

            unfixed[i] = new ArrayList<>(cap);
            fixed[i] = new ArrayList<>(cap);
        }

        for (ClickHandlerBase<?> item : guiItems) {
            ClickHandlerBase<?> base = handlePageSwitcher(item);

            if (base.fixedSlot() == -1) unfixed[base.page()].add(base);
            else fixed[base.page()].add(base);
        }

        for (int i = 0; i < highPage + 1; i++) {
            unfixed[i].trimToSize();
            fixed[i].trimToSize();
        }

        this.unfixedItems = unfixed;
        this.fixedItems = fixed;
    }

    private <T> ClickHandlerBase<T> handlePageSwitcher(ClickHandlerBase<T> item) {
        if (item.pageDirection() == ClickHandlerBase.SwitchDirection.NONE) {
            return item;
        }

        return new ClickHandlerBase<>(item.data(), item.playerMap()) {
            @Override
            public @NotNull ItemStack getItemStack() {
                return item.getItemStack();
            }

            @Override
            public boolean shouldRedraw() {
                return item.shouldRedraw();
            }

            @Override
            public void handleClick(@NotNull Gui owner, @NotNull Player player, int slot, @NotNull ClickType clickType) {
                item.handleClick(owner, player, slot, clickType);

                if (clickType == ClickType.LEFT_CLICK) {
                    int curPage = page();
                    int nextPage = switch (item.pageDirection()) {
                        case NONE -> curPage;
                        case NEXT -> curPage + 1;
                        case PREV -> curPage - 1;
                    };

                    if (nextPage != curPage && nextPage >= 0 && nextPage < unfixedItems.length) {
                        player.openInventory(buildGui(player, nextPage));
                    }
                }
            }

            @Override
            public void itemTick(@NotNull Gui gui, long time, int slot) {
                item.itemTick(gui, time, slot);
            }

            @Override
            public int fixedSlot() {
                return item.fixedSlot();
            }

            @Override
            public int page() {
                return item.page();
            }

            @Override
            public void tick(long time) {
                item.tick(time);
            }

            @Override
            public void initialize(@NotNull Shop shop) {
                item.initialize(shop);
            }

            @Override
            public void onRemove(@NotNull Gui owner, int slot) {
                item.onRemove(owner, slot);
            }

            @Override
            public void onReplace(@NotNull Gui owner, @NotNull GuiItem newItem, int slot) {
                item.onReplace(owner, newItem, slot);
            }

            @Override
            public @NotNull SwitchDirection pageDirection() {
                return item.pageDirection();
            }

            @Override
            public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
                return item.handleInteraction(interaction);
            }
        };
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        for (ArrayList<ClickHandlerBase<?>> handlers : unfixedItems) ShopInteractor.initialize(handlers, shop);
        for (ArrayList<ClickHandlerBase<?>> handlers : fixedItems) ShopInteractor.initialize(handlers, shop);
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        interaction.player().module().getPlayerView().getPlayer().ifPresent(player -> {
            if (data.startPage >= 0 && data.startPage < fixedItems.length)
                player.openInventory(buildGui(player, data.startPage));
        });

        return true;
    }

    private Gui buildGui(Player player, int page) {
        ArrayList<ClickHandlerBase<?>> unfixed = this.unfixedItems[page];
        ArrayList<ClickHandlerBase<?>> fixed = this.fixedItems[page];

        Gui gui = Gui.builder(data.inventoryType, slotDistributor, player)
            .withItems(unfixed)
            .setDynamic(data.dynamic)
            .withTitle(data.title)
            .build();

        for (ClickHandlerBase<?> fixedItem : fixed) {
            int slot = fixedItem.fixedSlot();
            if (gui.canInsert(slot)) gui.insertItem(fixedItem, slot);
        }

        if (data.dynamic) {
            ticking.add(gui);
        }

        return gui;
    }

    @Override
    public void tick(long time) {
        // do interactor ticks
        for (ArrayList<ClickHandlerBase<?>> handlers : unfixedItems) ShopInteractor.tick(handlers, time);
        for (ArrayList<ClickHandlerBase<?>> handlers : fixedItems) ShopInteractor.tick(handlers, time);

        if (data.dynamic) {
            // do item ticks
            ticking.removeIf(gui -> {
                boolean isOpened = gui.getOwner().getOpenInventory() == gui;
                if (!isOpened) {
                    return true;
                }

                gui.tick(time);
                return false;
            });
        }
    }

    @DataObject
    @Default("""
        {
          dynamic=false,
          startPage=0
        }
        """)
    public record Data(
        @NotNull Component title,
        @NotNull InventoryType inventoryType,
        boolean dynamic,
        int startPage) {

    }
}