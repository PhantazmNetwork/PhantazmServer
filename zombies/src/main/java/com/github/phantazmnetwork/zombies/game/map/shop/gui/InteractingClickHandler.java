package com.github.phantazmnetwork.zombies.game.map.shop.gui;

import com.github.phantazmnetwork.core.config.processor.ItemStackConfigProcessors;
import com.github.phantazmnetwork.core.gui.Gui;
import com.github.phantazmnetwork.core.gui.ItemUpdater;
import com.github.phantazmnetwork.zombies.game.map.BasicPlayerInteraction;
import com.github.phantazmnetwork.zombies.game.map.shop.InteractionTypes;
import com.github.phantazmnetwork.zombies.game.map.shop.interactor.ShopInteractor;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Model("zombies.map.shop.gui.click_handler.interacting")
public class InteractingClickHandler extends ClickHandlerBase<InteractingClickHandler.Data> {
    private final ItemUpdater itemUpdater;
    private final ShopInteractor clickInteractor;

    private ItemStack itemStack;
    private boolean redraw;

    @ProcessorMethod
    public static ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            private static final ConfigProcessor<ItemStack> ITEM_STACK_PROCESSOR = ItemStackConfigProcessors.snbt();

            private static final ConfigProcessor<Set<ClickType>> CLICK_TYPE_SET_PROCESSOR =
                    ConfigProcessor.enumProcessor(ClickType.class).setProcessor();

            @Override
            public Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                ItemStack initialItem = ITEM_STACK_PROCESSOR.dataFromElement(element.getElementOrThrow("initialItem"));
                Set<ClickType> clickTypes =
                        CLICK_TYPE_SET_PROCESSOR.dataFromElement(element.getElementOrThrow("clickTypes"));
                boolean blacklist = element.getBooleanOrThrow("blacklist");
                String itemUpdater = element.getStringOrThrow("itemUpdater");
                String clickInteractor = element.getStringOrThrow("clickInteractor");
                return new Data(initialItem, clickTypes, blacklist, itemUpdater, clickInteractor);
            }

            @Override
            public @NotNull ConfigElement elementFromData(Data data) throws ConfigProcessException {
                return ConfigNode.of("initialItem", ITEM_STACK_PROCESSOR.elementFromData(data.initialItem),
                        "clickTypes", CLICK_TYPE_SET_PROCESSOR.elementFromData(data.clickTypes), "blacklist",
                        data.blacklist, "itemUpdater", data.itemUpdater, "clickInteractor", data.clickInteractor);
            }
        };
    }

    @FactoryMethod
    public InteractingClickHandler(@NotNull Data data,
            @NotNull @Dependency("zombies.dependency.map_object.player_function")
            Function<? super UUID, ? extends ZombiesPlayer> playerFunction,
            @NotNull @DataName("updater") ItemUpdater itemUpdater,
            @NotNull @DataName("interactor") ShopInteractor clickInteractor) {
        super(data, playerFunction);
        this.itemUpdater = Objects.requireNonNull(itemUpdater, "itemUpdater");
        this.clickInteractor = Objects.requireNonNull(clickInteractor, "clickInteractor");

        this.itemStack = data.initialItem;
    }

    @Override
    public void handleClick(@NotNull Gui owner, @NotNull Player player, int slot, @NotNull ClickType clickType) {
        ZombiesPlayer zombiesPlayer = playerFunction.apply(player.getUuid());
        if (zombiesPlayer != null && data.clickTypes.contains(clickType) != data.blacklist) {
            clickInteractor.handleInteraction(
                    new BasicPlayerInteraction(zombiesPlayer, InteractionTypes.CLICK_INVENTORY));
        }
    }

    @Override
    public void tick(long time) {
        if (itemUpdater.hasUpdate(time, itemStack)) {
            itemStack = itemUpdater.update(time, itemStack);
            redraw = true;
        }
    }

    @Override
    public @NotNull ItemStack getItemStack() {
        redraw = false;
        return itemStack;
    }

    @Override
    public boolean shouldRedraw() {
        return redraw;
    }

    @DataObject
    public record Data(@NotNull ItemStack initialItem,
                       @NotNull Set<ClickType> clickTypes,
                       boolean blacklist,
                       @NotNull @DataPath("updater") String itemUpdater,
                       @NotNull @DataPath("interactor") String clickInteractor) {
    }
}
