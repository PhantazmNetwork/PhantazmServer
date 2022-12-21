package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.text.Component;
import net.minestom.server.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.gui.Gui;
import org.phantazm.core.gui.GuiItem;
import org.phantazm.core.gui.SlotDistributor;
import org.phantazm.zombies.map.shop.PlayerInteraction;

import java.util.List;
import java.util.Objects;

@Model("zombies.map.shop.interactor.open_gui")
public class OpenGuiInteractor extends InteractorBase<OpenGuiInteractor.Data> {
    private final SlotDistributor slotDistributor;
    private final List<GuiItem> guiItems;

    @FactoryMethod
    public OpenGuiInteractor(@NotNull Data data,
            @NotNull @Dependency("zombies.dependency.map_object.slot_distributor") SlotDistributor slotDistributor,
            @NotNull @DataName("items") List<GuiItem> guiItems) {
        super(data);
        this.slotDistributor = Objects.requireNonNull(slotDistributor, "slotDistributor");
        this.guiItems = Objects.requireNonNull(guiItems, "guiItems");
    }

    @Override
    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        interaction.player().getModule().getPlayerView().getPlayer().ifPresent(player -> player.openInventory(
                Gui.builder(data.inventoryType, slotDistributor).setDynamic(data.dynamic).withItems(guiItems)
                        .withTitle(data.title).build()));
    }

    @DataObject
    public record Data(@NotNull Component title,
                       @NotNull InventoryType inventoryType,
                       @NotNull @DataPath("items") List<String> guiItems,
                       boolean dynamic) {

    }
}
