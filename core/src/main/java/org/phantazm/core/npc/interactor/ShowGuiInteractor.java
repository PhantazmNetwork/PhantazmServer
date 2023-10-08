package org.phantazm.core.npc.interactor;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.MonoComponent;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.gui.BasicSlotDistributor;
import org.phantazm.core.gui.Gui;
import org.phantazm.core.gui.GuiItem;

import java.util.ArrayList;
import java.util.List;

@Model("npc.interactor.show_gui")
@Cache
public class ShowGuiInteractor implements MonoComponent<@NotNull NPCInteractor> {
    private final Data data;
    private final List<MonoComponent<GuiItem>> guiItems;

    @FactoryMethod
    public ShowGuiInteractor(@NotNull Data data, @NotNull @Child("gui_items") List<MonoComponent<GuiItem>> guiItems) {
        this.data = data;
        this.guiItems = guiItems;
    }

    @Override
    public @NotNull NPCInteractor apply(@NotNull InjectionStore injectionStore) {
        List<GuiItem> items = new ArrayList<>(guiItems.size());
        for (MonoComponent<GuiItem> component : guiItems) {
            items.add(component.apply(injectionStore));
        }

        return new Internal(data, items);
    }

    private record Internal(Data data,
        List<GuiItem> guiItems) implements NPCInteractor {
        @Override
        public void interact(@NotNull Player player) {
            player.openInventory(
                Gui.builder(data.inventoryType, new BasicSlotDistributor(data.padding)).withItems(guiItems).build());
        }
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
