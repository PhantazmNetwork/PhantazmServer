package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.text.Component;
import net.minestom.server.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.gui.Gui;
import org.phantazm.core.gui.SlotDistributor;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.map.shop.gui.ClickHandlerBase;

import java.util.List;
import java.util.Objects;

@Model("zombies.map.shop.interactor.open_gui")
@Cache(false)
public class OpenGuiInteractor extends InteractorBase<OpenGuiInteractor.Data> {
    private final SlotDistributor slotDistributor;
    private final List<ClickHandlerBase<?>> guiItems;

    @FactoryMethod
    public OpenGuiInteractor(@NotNull Data data, @NotNull SlotDistributor slotDistributor,
            @NotNull @Child("items") List<ClickHandlerBase<?>> guiItems) {
        super(data);
        this.slotDistributor = Objects.requireNonNull(slotDistributor, "slotDistributor");
        this.guiItems = Objects.requireNonNull(guiItems, "guiItems");
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        ShopInteractor.initialize(guiItems, shop);
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        interaction.player().module().getPlayerView().getPlayer().ifPresent(player -> player.openInventory(
                Gui.builder(data.inventoryType, slotDistributor).setDynamic(data.dynamic).withItems(guiItems)
                        .withTitle(data.title).build()));
        return true;
    }

    @DataObject
    public record Data(@NotNull Component title,
                       @NotNull InventoryType inventoryType,
                       @NotNull @ChildPath("items") List<String> guiItems,
                       boolean dynamic) {

    }
}
