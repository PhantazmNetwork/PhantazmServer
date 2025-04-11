package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.gui.Gui;
import org.phantazm.core.gui.SlotDistributor;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.map.shop.gui.ClickHandlerBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Model("zombies.map.shop.interactor.open_gui")
@Cache(false)
public class OpenGuiInteractor extends InteractorBase<OpenGuiInteractor.Data> {
    private final SlotDistributor slotDistributor;

    private final List<ClickHandlerBase<?>> unfixedItems;
    private final List<ClickHandlerBase<?>> fixedItems;

    private final List<Gui> ticking = new CopyOnWriteArrayList<>();

    @FactoryMethod
    public OpenGuiInteractor(@NotNull Data data, @NotNull SlotDistributor slotDistributor,
        @NotNull @Child("guiItems") List<ClickHandlerBase<?>> guiItems) {
        super(data);
        this.slotDistributor = Objects.requireNonNull(slotDistributor);

        List<ClickHandlerBase<?>> unfixed = new ArrayList<>();
        List<ClickHandlerBase<?>> fixed = new ArrayList<>();
        for (ClickHandlerBase<?> item : guiItems) {
            if (item.fixedSlot() == -1) unfixed.add(item);
            else fixed.add(item);
        }

        this.unfixedItems = List.copyOf(unfixed);
        this.fixedItems = List.copyOf(fixed);
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        ShopInteractor.initialize(unfixedItems, shop);
        ShopInteractor.initialize(fixedItems, shop);
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        interaction.player().module().getPlayerView().getPlayer().ifPresent(player -> player.openInventory(buildGui(player)));
        return true;
    }

    private Gui buildGui(Player player) {
        Gui gui = Gui.builder(data.inventoryType, slotDistributor, player)
            .withItems(unfixedItems)
            .setDynamic(data.dynamic)
            .withTitle(data.title)
            .build();

        for (ClickHandlerBase<?> fixed : fixedItems) {
            int slot = fixed.fixedSlot();
            if (gui.canInsert(slot)) gui.insertItem(fixed, slot);
        }

        if (data.dynamic) {
            ticking.add(gui);
        }

        return gui;
    }

    @Override
    public void tick(long time) {
        // do interactor ticks
        ShopInteractor.tick(unfixedItems, time);
        ShopInteractor.tick(fixedItems, time);

        // do item ticks
        if (data.dynamic) {
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
          dynamic=false
        }
        """)
    public record Data(
        @NotNull Component title,
        @NotNull InventoryType inventoryType,
        boolean dynamic) {

    }
}