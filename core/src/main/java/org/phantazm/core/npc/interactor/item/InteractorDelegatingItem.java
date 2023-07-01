package org.phantazm.core.npc.interactor.item;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.gui.Gui;
import org.phantazm.core.gui.GuiItem;
import org.phantazm.core.npc.interactor.Interactor;

@Model("npc.interactor.gui.item")
@Cache(false)
public class InteractorDelegatingItem implements GuiItem {
    private final Data data;
    private final Interactor interactor;

    @FactoryMethod
    public InteractorDelegatingItem(@NotNull Data data, @NotNull @Child("interactor") Interactor interactor) {
        this.data = data;
        this.interactor = interactor;
    }

    @Override
    public void handleClick(@NotNull Gui owner, @NotNull Player player, int slot, @NotNull ClickType clickType) {
        if (clickType == ClickType.LEFT_CLICK) {
            interactor.interact(player);

            if (data.closeOnClick) {
                player.closeInventory();
            }
        }
    }

    @Override
    public void onRemove(@NotNull Gui owner, int slot) {

    }

    @Override
    public void onReplace(@NotNull Gui owner, @NotNull GuiItem newItem, int slot) {

    }

    @Override
    public @NotNull ItemStack getItemStack() {
        return data.itemStack;
    }

    @Override
    public boolean shouldRedraw() {
        return false;
    }

    @DataObject
    public record Data(@NotNull ItemStack itemStack,
                       boolean closeOnClick,
                       @NotNull @ChildPath("interactor") String interactor) {
        @Default("closeOnClick")
        public static @NotNull ConfigElement defaultCloseOnClick() {
            return ConfigPrimitive.of(true);
        }
    }
}
