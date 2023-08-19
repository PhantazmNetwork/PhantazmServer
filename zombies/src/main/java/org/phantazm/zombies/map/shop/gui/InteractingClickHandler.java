package org.phantazm.zombies.map.shop.gui;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.gui.Gui;
import org.phantazm.core.gui.ItemUpdater;
import org.phantazm.core.item.UpdatingItem;
import org.phantazm.zombies.map.BasicPlayerInteraction;
import org.phantazm.zombies.map.shop.InteractionTypes;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.map.shop.interactor.ShopInteractor;
import org.phantazm.zombies.player.ZombiesPlayer;

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
    public InteractingClickHandler(@NotNull Data data, @NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap,
            @NotNull @Child("updating_item") UpdatingItem updatingItem,
            @NotNull @Child("click_interactor") ShopInteractor clickInteractor) {
        super(data, playerMap);
        this.updatingItem = Objects.requireNonNull(updatingItem);
        this.clickInteractor = Objects.requireNonNull(clickInteractor);
        this.itemStack = updatingItem.currentItem();
    }

    @Override
    public void handleClick(@NotNull Gui owner, @NotNull Player player, int slot, @NotNull ClickType clickType) {
        ZombiesPlayer zombiesPlayer = playerMap.get(player.getUuid());
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
    public void tick(long time) {
        if (updatingItem.hasUpdate(time, itemStack)) {
            itemStack = updatingItem.update(time, itemStack);
            redraw = true;
        }

        clickInteractor.tick(time);
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
                       @NotNull @ChildPath("updating_item") String updatingItem,
                       @NotNull @ChildPath("click_interactor") String clickInteractor,
                       boolean closeOnInteract) {
        @Default("closeOnInteract")
        public static @NotNull ConfigElement defaultCloseOnInteract() {
            return ConfigPrimitive.of(true);
        }
    }
}
