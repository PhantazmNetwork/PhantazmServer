package org.phantazm.zombies.map.shop.gui;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.gui.Gui;
import org.phantazm.core.gui.ItemUpdater;
import org.phantazm.core.item.UpdatingItem;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.map.BasicPlayerInteraction;
import org.phantazm.zombies.map.shop.InteractionTypes;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.map.shop.interactor.ShopInteractor;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Model("zombies.map.shop.gui.click_handler.interacting")
@Cache(false)
public class InteractingClickHandler extends ClickHandlerBase<InteractingClickHandler.Data> {
    private final ItemUpdater updatingItem;
    private final ShopInteractor clickInteractor;

    @FactoryMethod
    public InteractingClickHandler(@NotNull Data data, @NotNull Map<PlayerView, ZombiesPlayer> playerMap,
        @NotNull @Child("updatingItem") UpdatingItem updatingItem,
        @NotNull @Child("clickInteractor") ShopInteractor clickInteractor) {
        super(data, playerMap);
        this.updatingItem = Objects.requireNonNull(updatingItem);
        this.clickInteractor = Objects.requireNonNull(clickInteractor);
    }

    @Override
    public void handleClick(@NotNull Gui owner, @NotNull Player player, int slot, @NotNull ClickType clickType) {
        ZombiesPlayer zombiesPlayer = playerMap.get(PlayerView.lookup(player.getUuid()));
        if (zombiesPlayer != null && data.clickTypes.contains(clickType) != data.blacklist) {
            boolean success = clickInteractor.handleInteraction(
                new BasicPlayerInteraction(zombiesPlayer, InteractionTypes.CLICK_INVENTORY));

            if (data.closeOnInteract && success) {
                player.closeInventory();
            }
        }
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        super.initialize(shop);

        clickInteractor.initialize(shop);
    }

    @Override
    public int fixedSlot() {
        return data.fixedSlot;
    }

    @Override
    public int page() {
        return data.page;
    }

    @Override
    public void itemTick(@NotNull Gui gui, long time, int slot) {
        ItemStack current = gui.getItemStack(slot);

        if (updatingItem.hasUpdate(gui, time, current)) {
            gui.setItemStack(slot, updatingItem.update(gui, time, current));
        }
    }

    @Override
    public void tick(long time) {
        clickInteractor.tick(time);
    }

    @Override
    public @NotNull SwitchDirection pageDirection() {
        return data.switchDirection;
    }

    @Override
    public @NotNull ItemStack getItemStack() {
        return ItemStack.AIR;
    }

    @Override
    public boolean shouldRedraw() {
        return false;
    }

    @Default("""
        {
          closeOnInteract=true,
          fixedSlot=-1,
          page=0,
          switchDirection='NONE'
        }
        """)
    @DataObject
    public record Data(
        @NotNull Set<ClickType> clickTypes,
        boolean blacklist,
        boolean closeOnInteract,
        int fixedSlot,
        int page,
        @NotNull SwitchDirection switchDirection) {
    }
}
