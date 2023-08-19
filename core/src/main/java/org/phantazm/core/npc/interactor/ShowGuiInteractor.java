package org.phantazm.core.npc.interactor;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.gui.BasicSlotDistributor;
import org.phantazm.core.gui.Gui;
import org.phantazm.core.gui.GuiItem;

import java.util.List;

@Model("npc.interactor.show_gui")
@Cache(false)
public class ShowGuiInteractor implements Interactor {
    private final Data data;
    private final List<GuiItem> guiItems;

    @FactoryMethod
    public ShowGuiInteractor(@NotNull Data data, @NotNull @Child("gui_items") List<GuiItem> guiItems) {
        this.data = data;
        this.guiItems = guiItems;
    }

    @Override
    public void interact(@NotNull Player player) {
        player.openInventory(
            Gui.builder(data.inventoryType, new BasicSlotDistributor(data.padding)).withItems(guiItems).build());
    }

    @DataObject
    public record Data(
        @NotNull InventoryType inventoryType,
        int padding,
        @NotNull @ChildPath("gui_items") List<String> guiItems) {
        @Default("inventoryType")
        public static @NotNull ConfigElement defaultInventoryType() {
            return ConfigPrimitive.of("CHEST_3_ROW");
        }

        @Default("padding")
        public static @NotNull ConfigElement defaultPadding() {
            return ConfigPrimitive.of(1);
        }
    }
}
