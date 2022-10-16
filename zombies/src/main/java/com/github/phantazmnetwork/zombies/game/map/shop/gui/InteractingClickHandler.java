package com.github.phantazmnetwork.zombies.game.map.shop.gui;

import com.github.phantazmnetwork.core.gui.Gui;
import com.github.phantazmnetwork.core.gui.ItemUpdater;
import com.github.phantazmnetwork.core.item.UpdatingItem;
import com.github.phantazmnetwork.zombies.game.map.BasicPlayerInteraction;
import com.github.phantazmnetwork.zombies.game.map.shop.InteractionTypes;
import com.github.phantazmnetwork.zombies.game.map.shop.interactor.ShopInteractor;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Model("zombies.map.shop.gui.click_handler.interacting")
@Cache(false)
public class InteractingClickHandler extends ClickHandlerBase<InteractingClickHandler.Data> {
    private final ItemUpdater updatingItem;
    private final ShopInteractor clickInteractor;

    private ItemStack itemStack;
    private boolean redraw;

    @FactoryMethod
    public InteractingClickHandler(@NotNull Data data, @NotNull @Dependency("zombies.dependency.map_object.player_map")
    Map<? super UUID, ? extends ZombiesPlayer> playerMap, @NotNull @DataName("updating_item") UpdatingItem updatingItem,
            @NotNull @DataName("click_interactor") ShopInteractor clickInteractor) {
        super(data, playerMap);
        this.updatingItem = Objects.requireNonNull(updatingItem, "updatingItem");
        this.clickInteractor = Objects.requireNonNull(clickInteractor, "clickInteractor");
        this.itemStack = updatingItem.currentItem();
    }

    @Override
    public void handleClick(@NotNull Gui owner, @NotNull Player player, int slot, @NotNull ClickType clickType) {
        ZombiesPlayer zombiesPlayer = playerMap.get(player.getUuid());
        if (zombiesPlayer != null && data.clickTypes.contains(clickType) != data.blacklist) {
            clickInteractor.handleInteraction(
                    new BasicPlayerInteraction(zombiesPlayer, InteractionTypes.CLICK_INVENTORY));
        }
    }

    @Override
    public void tick(long time) {
        if (updatingItem.hasUpdate(time, itemStack)) {
            itemStack = updatingItem.update(time, itemStack);
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
    public record Data(@NotNull Set<ClickType> clickTypes,
                       boolean blacklist,
                       @NotNull @DataPath("updating_item") String updatingItem,
                       @NotNull @DataPath("click_interactor") String clickInteractor) {
    }
}
