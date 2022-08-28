package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.phantazmnetwork.core.gui.Gui;
import com.github.phantazmnetwork.core.gui.GuiItem;
import com.github.phantazmnetwork.core.gui.SlotDistributor;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.text.Component;
import net.minestom.server.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@Model("zombies.map.shop.interactor.open_gui")
public class OpenGuiInteractor extends InteractorBase<OpenGuiInteractor.Data> {
    private final SlotDistributor slotDistributor;
    private final List<GuiItem> guiItems;

    @ProcessorMethod
    public static ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            private static final ConfigProcessor<Component> COMPONENT_PROCESSOR = ConfigProcessors.component();
            private static final ConfigProcessor<InventoryType> INVENTORY_TYPE_PROCESSOR =
                    ConfigProcessor.enumProcessor(InventoryType.class);
            private static final ConfigProcessor<List<String>> STRING_LIST_PROCESSOR =
                    ConfigProcessor.STRING.listProcessor();

            @Override
            public Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Component title = COMPONENT_PROCESSOR.dataFromElement(element.getElementOrThrow("title"));
                InventoryType inventoryType =
                        INVENTORY_TYPE_PROCESSOR.dataFromElement(element.getElementOrThrow("inventoryType"));
                List<String> guiItems = STRING_LIST_PROCESSOR.dataFromElement(element.getElementOrThrow("guiItems"));
                boolean dynamic = element.getBooleanOrThrow("dynamic");
                return new Data(title, inventoryType, guiItems, dynamic);
            }

            @Override
            public @NotNull ConfigElement elementFromData(Data data) throws ConfigProcessException {
                return ConfigNode.of("title", COMPONENT_PROCESSOR.elementFromData(data.title), "inventoryType",
                        INVENTORY_TYPE_PROCESSOR.elementFromData(data.inventoryType), "guiItems",
                        STRING_LIST_PROCESSOR.elementFromData(data.guiItems), "dynamic", data.dynamic);
            }
        };
    }

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
        interaction.player().getPlayerView().getPlayer().ifPresent(player -> player.openInventory(
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
